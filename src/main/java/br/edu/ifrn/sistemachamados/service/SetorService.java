package br.edu.ifrn.sistemachamados.service;

import br.edu.ifrn.sistemachamados.dto.PaginaResponse;
import br.edu.ifrn.sistemachamados.dto.SetorRequest;
import br.edu.ifrn.sistemachamados.dto.SetorResponse;
import br.edu.ifrn.sistemachamados.exception.RecursoNaoEncontradoException;
import br.edu.ifrn.sistemachamados.exception.RegraNegocioException;
import br.edu.ifrn.sistemachamados.model.Setor;
import br.edu.ifrn.sistemachamados.repository.CategoriaRepository;
import br.edu.ifrn.sistemachamados.repository.ChamadoRepository;
import br.edu.ifrn.sistemachamados.repository.SetorRepository;
import br.edu.ifrn.sistemachamados.repository.UsuarioRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class SetorService {

    private final SetorRepository setorRepository;
    private final CategoriaRepository categoriaRepository;
    private final UsuarioRepository usuarioRepository;
    private final ChamadoRepository chamadoRepository;

    @Transactional
    public SetorResponse criar(SetorRequest request) {
        if (setorRepository.existsBySigla(request.sigla())) {
            throw new RegraNegocioException("Já existe um setor com a sigla " + request.sigla());
        }
        Setor setor = new Setor();
        preencher(setor, request);
        return SetorResponse.de(setorRepository.save(setor));
    }

    public PaginaResponse<SetorResponse> listar(Pageable pageable) {
        return PaginaResponse.de(setorRepository.findAll(pageable).map(SetorResponse::de));
    }

    public SetorResponse buscarPorId(Long id) {
        return SetorResponse.de(buscarEntidade(id));
    }

    @Transactional
    public SetorResponse atualizar(Long id, SetorRequest request) {
        Setor setor = buscarEntidade(id);
        if (setorRepository.existsBySiglaAndIdNot(request.sigla(), id)) {
            throw new RegraNegocioException("Já existe um setor com a sigla " + request.sigla());
        }
        preencher(setor, request);
        // Entidade gerenciada: as alterações são gravadas no commit da transação
        return SetorResponse.de(setor);
    }

    @Transactional
    public void excluir(Long id) {
        Setor setor = buscarEntidade(id);
        // Regra 8
        if (chamadoRepository.existsByCategoriaSetorId(id)) {
            throw new RegraNegocioException("Não é possível excluir um setor que possui chamados vinculados");
        }
        // Sem estas verificações o banco recusaria a exclusão pelas chaves estrangeiras
        if (categoriaRepository.existsBySetorId(id)) {
            throw new RegraNegocioException("Não é possível excluir um setor que possui categorias vinculadas");
        }
        if (usuarioRepository.existsBySetorId(id)) {
            throw new RegraNegocioException("Não é possível excluir um setor que possui técnicos vinculados");
        }
        setorRepository.delete(setor);
    }

    // Também usado pelos services de Categoria e Usuario para validar o setorId recebido
    public Setor buscarEntidade(Long id) {
        return setorRepository.findById(id)
                .orElseThrow(() -> new RecursoNaoEncontradoException("Setor não encontrado: " + id));
    }

    private void preencher(Setor setor, SetorRequest request) {
        setor.setNome(request.nome());
        setor.setSigla(request.sigla());
    }
}
