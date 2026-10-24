package br.edu.ifrn.sistemachamados.repository;

import br.edu.ifrn.sistemachamados.dto.CategoriaFiltro;
import br.edu.ifrn.sistemachamados.model.Categoria;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.util.StringUtils;

// Monta o WHERE de GET /categorias só com os filtros informados
public final class CategoriaSpecifications {

    private CategoriaSpecifications() {
    }

    public static Specification<Categoria> comFiltro(CategoriaFiltro filtro) {
        Specification<Categoria> spec = Specification.unrestricted();
        if (StringUtils.hasText(filtro.nome())) {
            spec = spec.and(nomeContem(filtro.nome()));
        }
        if (filtro.setorId() != null) {
            spec = spec.and(doSetor(filtro.setorId()));
        }
        return spec;
    }

    private static Specification<Categoria> nomeContem(String nome) {
        return (root, query, cb) -> cb.like(cb.lower(root.get("nome")), "%" + nome.toLowerCase() + "%");
    }

    private static Specification<Categoria> doSetor(Long setorId) {
        return (root, query, cb) -> cb.equal(root.get("setor").get("id"), setorId);
    }
}
