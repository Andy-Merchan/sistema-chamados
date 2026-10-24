package br.edu.ifrn.sistemachamados.service;

import br.edu.ifrn.sistemachamados.dto.PaginaResponse;
import br.edu.ifrn.sistemachamados.dto.UsuarioFiltro;
import br.edu.ifrn.sistemachamados.dto.UsuarioRequest;
import br.edu.ifrn.sistemachamados.dto.UsuarioResponse;
import br.edu.ifrn.sistemachamados.exception.RecursoNaoEncontradoException;
import br.edu.ifrn.sistemachamados.exception.RegraNegocioException;
import br.edu.ifrn.sistemachamados.model.Usuario;
import br.edu.ifrn.sistemachamados.repository.UsuarioRepository;
import br.edu.ifrn.sistemachamados.repository.UsuarioSpecifications;
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

    public PaginaResponse<UsuarioResponse> listar(UsuarioFiltro filtro, Pageable pageable) {
        return PaginaResponse.de(usuarioRepository.findAll(UsuarioSpecifications.comFiltro(filtro), pageable).map(UsuarioResponse::de));
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
        // A coerência entre tipo e setorId já foi validada no DTO; aqui só falta garantir que o setor existe (404)
        usuario.setSetor(request.setorId() != null ? setorService.buscarEntidade(request.setorId()) : null);
    }
}
