package br.edu.ifrn.sistemachamados.service;

import br.edu.ifrn.sistemachamados.dto.CategoriaRequest;
import br.edu.ifrn.sistemachamados.dto.CategoriaResponse;
import br.edu.ifrn.sistemachamados.dto.PaginaResponse;
import br.edu.ifrn.sistemachamados.exception.RecursoNaoEncontradoException;
import br.edu.ifrn.sistemachamados.exception.RegraNegocioException;
import br.edu.ifrn.sistemachamados.model.Categoria;
import br.edu.ifrn.sistemachamados.repository.CategoriaRepository;
import br.edu.ifrn.sistemachamados.repository.ChamadoRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class CategoriaService {

    private final CategoriaRepository categoriaRepository;
    private final ChamadoRepository chamadoRepository;
    private final SetorService setorService;

    @Transactional
    public CategoriaResponse criar(CategoriaRequest request) {
        Categoria categoria = new Categoria();
        preencher(categoria, request);
        return CategoriaResponse.de(categoriaRepository.save(categoria));
    }

    public PaginaResponse<CategoriaResponse> listar(Pageable pageable) {
        return PaginaResponse.de(categoriaRepository.findAll(pageable).map(CategoriaResponse::de));
    }

    public PaginaResponse<CategoriaResponse> listarPorSetor(Long setorId, Pageable pageable) {
        // Garante 404 para setor inexistente em vez de uma lista vazia
        setorService.buscarEntidade(setorId);
        return PaginaResponse.de(categoriaRepository.findBySetorId(setorId, pageable).map(CategoriaResponse::de));
    }

    public CategoriaResponse buscarPorId(Long id) {
        return CategoriaResponse.de(buscarEntidade(id));
    }

    @Transactional
    public CategoriaResponse atualizar(Long id, CategoriaRequest request) {
        Categoria categoria = buscarEntidade(id);
        preencher(categoria, request);
        return CategoriaResponse.de(categoria);
    }

    @Transactional
    public void excluir(Long id) {
        Categoria categoria = buscarEntidade(id);
        // Regra 8
        if (chamadoRepository.existsByCategoriaId(id)) {
            throw new RegraNegocioException("Não é possível excluir uma categoria que possui chamados vinculados");
        }
        categoriaRepository.delete(categoria);
    }

    public Categoria buscarEntidade(Long id) {
        return categoriaRepository.findById(id)
                .orElseThrow(() -> new RecursoNaoEncontradoException("Categoria não encontrada: " + id));
    }

    private void preencher(Categoria categoria, CategoriaRequest request) {
        categoria.setNome(request.nome());
        categoria.setDescricao(request.descricao());
        categoria.setSetor(setorService.buscarEntidade(request.setorId()));
    }
}
