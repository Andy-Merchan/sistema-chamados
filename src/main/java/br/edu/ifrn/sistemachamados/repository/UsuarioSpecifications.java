package br.edu.ifrn.sistemachamados.repository;

import br.edu.ifrn.sistemachamados.dto.UsuarioFiltro;
import br.edu.ifrn.sistemachamados.model.Usuario;
import br.edu.ifrn.sistemachamados.model.enums.TipoUsuario;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.util.StringUtils;

// Monta o WHERE de GET /usuarios só com os filtros informados
public final class UsuarioSpecifications {

    private UsuarioSpecifications() {
    }

    public static Specification<Usuario> comFiltro(UsuarioFiltro filtro) {
        Specification<Usuario> spec = Specification.unrestricted();
        if (StringUtils.hasText(filtro.nome())) {
            spec = spec.and(nomeContem(filtro.nome()));
        }
        if (filtro.tipo() != null) {
            spec = spec.and(doTipo(filtro.tipo()));
        }
        if (filtro.setorId() != null) {
            spec = spec.and(doSetor(filtro.setorId()));
        }
        return spec;
    }

    private static Specification<Usuario> nomeContem(String nome) {
        return (root, query, cb) -> cb.like(cb.lower(root.get("nome")), "%" + nome.toLowerCase() + "%");
    }

    private static Specification<Usuario> doTipo(TipoUsuario tipo) {
        return (root, query, cb) -> cb.equal(root.get("tipo"), tipo);
    }

    private static Specification<Usuario> doSetor(Long setorId) {
        return (root, query, cb) -> cb.equal(root.get("setor").get("id"), setorId);
    }
}
