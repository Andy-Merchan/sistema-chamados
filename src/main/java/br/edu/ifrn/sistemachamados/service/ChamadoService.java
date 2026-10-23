package br.edu.ifrn.sistemachamados.service;

import br.edu.ifrn.sistemachamados.dto.AssumirChamadoRequest;
import br.edu.ifrn.sistemachamados.dto.CancelarChamadoRequest;
import br.edu.ifrn.sistemachamados.dto.ChamadoDetalheResponse;
import br.edu.ifrn.sistemachamados.dto.ChamadoRequest;
import br.edu.ifrn.sistemachamados.dto.ChamadoResponse;
import br.edu.ifrn.sistemachamados.dto.InteracaoRequest;
import br.edu.ifrn.sistemachamados.dto.InteracaoResponse;
import br.edu.ifrn.sistemachamados.dto.PaginaResponse;
import br.edu.ifrn.sistemachamados.dto.ResolverChamadoRequest;
import br.edu.ifrn.sistemachamados.exception.RecursoNaoEncontradoException;
import br.edu.ifrn.sistemachamados.exception.RegraNegocioException;
import br.edu.ifrn.sistemachamados.model.Chamado;
import br.edu.ifrn.sistemachamados.model.Interacao;
import br.edu.ifrn.sistemachamados.model.Usuario;
import br.edu.ifrn.sistemachamados.model.enums.Prioridade;
import br.edu.ifrn.sistemachamados.model.enums.StatusChamado;
import br.edu.ifrn.sistemachamados.model.enums.TipoUsuario;
import br.edu.ifrn.sistemachamados.repository.ChamadoRepository;
import br.edu.ifrn.sistemachamados.repository.InteracaoRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;

// Concentra as regras de negócio 1 a 7 e o ciclo de vida do chamado:
// ABERTO -> EM_ATENDIMENTO -> RESOLVIDO e ABERTO -> CANCELADO
@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class ChamadoService {

    private final ChamadoRepository chamadoRepository;
    private final InteracaoRepository interacaoRepository;
    private final UsuarioService usuarioService;
    private final CategoriaService categoriaService;

    @Transactional
    public ChamadoResponse abrir(ChamadoRequest request) {
        Usuario solicitante = usuarioService.buscarEntidade(request.solicitanteId());
        // Regra 2
        if (solicitante.getTipo() != TipoUsuario.SOLICITANTE) {
            throw new RegraNegocioException("Apenas usuários do tipo SOLICITANTE podem abrir chamados");
        }

        Chamado chamado = new Chamado();
        chamado.setTitulo(request.titulo());
        chamado.setDescricao(request.descricao());
        chamado.setLocal(request.local());
        chamado.setCategoria(categoriaService.buscarEntidade(request.categoriaId()));
        chamado.setSolicitante(solicitante);
        // Regra 1: valores definidos pelo sistema, não pelo cliente
        chamado.setStatus(StatusChamado.ABERTO);
        chamado.setDataAbertura(LocalDateTime.now());
        chamado.setPrioridade(request.prioridade() != null ? request.prioridade() : Prioridade.MEDIA);
        return ChamadoResponse.de(chamadoRepository.save(chamado));
    }

    public PaginaResponse<ChamadoResponse> listar(Pageable pageable) {
        return PaginaResponse.de(chamadoRepository.findAll(pageable).map(ChamadoResponse::de));
    }

    public PaginaResponse<ChamadoResponse> listarPorUsuario(Long usuarioId, Pageable pageable) {
        // Garante 404 para usuário inexistente em vez de uma lista vazia
        usuarioService.buscarEntidade(usuarioId);
        return PaginaResponse.de(chamadoRepository
                .findBySolicitanteIdOrTecnicoId(usuarioId, usuarioId, pageable)
                .map(ChamadoResponse::de));
    }

    public ChamadoDetalheResponse buscarPorId(Long id) {
        return ChamadoDetalheResponse.de(buscarEntidade(id));
    }

    @Transactional
    public ChamadoResponse assumir(Long id, AssumirChamadoRequest request) {
        Chamado chamado = buscarEntidade(id);
        Usuario tecnico = usuarioService.buscarEntidade(request.tecnicoId());

        // Regra 3
        validarNaoFinalizado(chamado);
        if (tecnico.getTipo() != TipoUsuario.TECNICO) {
            throw new RegraNegocioException("Apenas usuários do tipo TECNICO podem assumir chamados");
        }
        if (chamado.getStatus() != StatusChamado.ABERTO) {
            throw new RegraNegocioException("Apenas chamados com status ABERTO podem ser assumidos");
        }
        Long setorDoChamado = chamado.getCategoria().getSetor().getId();
        if (!setorDoChamado.equals(tecnico.getSetor().getId())) {
            throw new RegraNegocioException("O técnico só pode assumir chamados de categorias do seu setor");
        }

        chamado.setTecnico(tecnico);
        chamado.setStatus(StatusChamado.EM_ATENDIMENTO);
        // Entidade gerenciada: as alterações são gravadas no commit da transação
        return ChamadoResponse.de(chamado);
    }

    @Transactional
    public ChamadoResponse resolver(Long id, ResolverChamadoRequest request) {
        Chamado chamado = buscarEntidade(id);
        Usuario tecnico = usuarioService.buscarEntidade(request.tecnicoId());

        // Regra 4 (a solução obrigatória é validada no DTO com @NotBlank)
        validarNaoFinalizado(chamado);
        if (chamado.getStatus() != StatusChamado.EM_ATENDIMENTO) {
            throw new RegraNegocioException("Apenas chamados com status EM_ATENDIMENTO podem ser resolvidos");
        }
        if (!chamado.getTecnico().getId().equals(tecnico.getId())) {
            throw new RegraNegocioException("Somente o técnico atribuído pode resolver o chamado");
        }

        chamado.setSolucao(request.solucao());
        chamado.setStatus(StatusChamado.RESOLVIDO);
        chamado.setDataFechamento(LocalDateTime.now());
        return ChamadoResponse.de(chamado);
    }

    @Transactional
    public ChamadoResponse cancelar(Long id, CancelarChamadoRequest request) {
        Chamado chamado = buscarEntidade(id);
        Usuario solicitante = usuarioService.buscarEntidade(request.solicitanteId());

        // Regra 5
        validarNaoFinalizado(chamado);
        if (!chamado.getSolicitante().getId().equals(solicitante.getId())) {
            throw new RegraNegocioException("Somente o próprio solicitante pode cancelar o chamado");
        }
        if (chamado.getStatus() != StatusChamado.ABERTO) {
            throw new RegraNegocioException("Apenas chamados com status ABERTO podem ser cancelados");
        }

        chamado.setStatus(StatusChamado.CANCELADO);
        return ChamadoResponse.de(chamado);
    }

    @Transactional
    public InteracaoResponse registrarInteracao(Long chamadoId, InteracaoRequest request) {
        Chamado chamado = buscarEntidade(chamadoId);
        Usuario autor = usuarioService.buscarEntidade(request.autorId());

        // Regra 6
        validarNaoFinalizado(chamado);
        // Regra 7
        if (!participaDoChamado(autor, chamado)) {
            throw new RegraNegocioException(
                    "Apenas o solicitante e o técnico atribuído podem registrar interações no chamado");
        }

        Interacao interacao = new Interacao();
        interacao.setTexto(request.texto());
        interacao.setDataHora(LocalDateTime.now());
        interacao.setChamado(chamado);
        interacao.setAutor(autor);
        return InteracaoResponse.de(interacaoRepository.save(interacao));
    }

    public Chamado buscarEntidade(Long id) {
        return chamadoRepository.findById(id)
                .orElseThrow(() -> new RecursoNaoEncontradoException("Chamado não encontrado: " + id));
    }

    // Regra 6: chamados RESOLVIDO ou CANCELADO não aceitam alterações nem novas interações
    private void validarNaoFinalizado(Chamado chamado) {
        StatusChamado status = chamado.getStatus();
        if (status == StatusChamado.RESOLVIDO || status == StatusChamado.CANCELADO) {
            throw new RegraNegocioException("Chamado com status " + status + " não aceita alterações");
        }
    }

    private boolean participaDoChamado(Usuario usuario, Chamado chamado) {
        boolean ehSolicitante = chamado.getSolicitante().getId().equals(usuario.getId());
        boolean ehTecnico = chamado.getTecnico() != null && chamado.getTecnico().getId().equals(usuario.getId());
        return ehSolicitante || ehTecnico;
    }
}
