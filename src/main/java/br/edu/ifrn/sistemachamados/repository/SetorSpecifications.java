package br.edu.ifrn.sistemachamados.repository;

import br.edu.ifrn.sistemachamados.dto.SetorFiltro;
import br.edu.ifrn.sistemachamados.model.Setor;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.util.StringUtils;

// Monta o WHERE de GET /setores só com os filtros informados
public final class SetorSpecifications {

    private SetorSpecifications() {
    }

    public static Specification<Setor> comFiltro(SetorFiltro filtro) {
        Specification<Setor> spec = Specification.unrestricted();
        if (StringUtils.hasText(filtro.nome())) {
            spec = spec.and(nomeContem(filtro.nome()));
        }
        return spec;
    }

    // LIKE sobre lower(nome): "contém", sem diferenciar maiúsculas
    private static Specification<Setor> nomeContem(String nome) {
        return (root, query, cb) -> cb.like(cb.lower(root.get("nome")), "%" + nome.toLowerCase() + "%");
    }
}
