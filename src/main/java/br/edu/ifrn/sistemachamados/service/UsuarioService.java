package br.edu.ifrn.sistemachamados.service;

import br.edu.ifrn.sistemachamados.dto.PaginaResponse;
import br.edu.ifrn.sistemachamados.dto.UsuarioRequest;
import br.edu.ifrn.sistemachamados.dto.UsuarioResponse;
import br.edu.ifrn.sistemachamados.exception.RecursoNaoEncontradoException;
import br.edu.ifrn.sistemachamados.exception.RegraNegocioException;
import br.edu.ifrn.sistemachamados.model.Setor;
import br.edu.ifrn.sistemachamados.model.Usuario;
import br.edu.ifrn.sistemachamados.model.enums.TipoUsuario;
import br.edu.ifrn.sistemachamados.repository.UsuarioRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class UsuarioService {

    private final UsuarioRepository usuarioRepository;
    private final SetorService setorService;

    @Transactional
    public UsuarioResponse criar(UsuarioRequest request) {
        if (usuarioRepository.existsByEmail(request.email())) {
            throw new RegraNegocioException("Já existe um usuário com o email " + request.email());
        }
        Usuario usuario = new Usuario();
        preencher(usuario, request);
        return UsuarioResponse.de(usuarioRepository.save(usuario));
    }

    public PaginaResponse<UsuarioResponse> listar(Pageable pageable) {
        return PaginaResponse.de(usuarioRepository.findAll(pageable).map(UsuarioResponse::de));
    }

    public UsuarioResponse buscarPorId(Long id) {
        return UsuarioResponse.de(buscarEntidade(id));
    }

    @Transactional
    public UsuarioResponse atualizar(Long id, UsuarioRequest request) {
        Usuario usuario = buscarEntidade(id);
        if (usuarioRepository.existsByEmailAndIdNot(request.email(), id)) {
            throw new RegraNegocioException("Já existe um usuário com o email " + request.email());
        }
        preencher(usuario, request);
        return UsuarioResponse.de(usuario);
    }

    public Usuario buscarEntidade(Long id) {
        return usuarioRepository.findById(id)
                .orElseThrow(() -> new RecursoNaoEncontradoException("Usuário não encontrado: " + id));
    }

    private void preencher(Usuario usuario, UsuarioRequest request) {
        usuario.setNome(request.nome());
        usuario.setEmail(request.email());
        usuario.setMatricula(request.matricula());
        usuario.setTipo(request.tipo());
        usuario.setSetor(resolverSetor(request));
    }

    // Técnico pertence obrigatoriamente a um setor; solicitante não pertence a nenhum
    private Setor resolverSetor(UsuarioRequest request) {
        if (request.tipo() == TipoUsuario.TECNICO) {
            if (request.setorId() == null) {
                throw new RegraNegocioException("Usuário do tipo TECNICO deve informar o setorId");
            }
            return setorService.buscarEntidade(request.setorId());
        }
        if (request.setorId() != null) {
            throw new RegraNegocioException("Usuário do tipo SOLICITANTE não pertence a um setor");
        }
        return null;
    }
}
