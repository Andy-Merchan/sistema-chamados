package br.edu.ifrn.sistemachamados.repository;

import br.edu.ifrn.sistemachamados.dto.ChamadoFiltro;
import br.edu.ifrn.sistemachamados.model.Chamado;
import br.edu.ifrn.sistemachamados.model.enums.Prioridade;
import br.edu.ifrn.sistemachamados.model.enums.StatusChamado;
import jakarta.persistence.criteria.JoinType;
import org.springframework.data.jpa.domain.Specification;

import java.time.LocalDate;

// Monta o WHERE das listagens de chamados só com os filtros informados
public final class ChamadoSpecifications {

    private ChamadoSpecifications() {
    }

    public static Specification<Chamado> comFiltro(ChamadoFiltro filtro) {
        Specification<Chamado> spec = Specification.unrestricted();
        if (filtro.status() != null) {
            spec = spec.and(doStatus(filtro.status()));
        }
        if (filtro.prioridade() != null) {
            spec = spec.and(daPrioridade(filtro.prioridade()));
        }
        if (filtro.setorId() != null) {
            spec = spec.and(doSetor(filtro.setorId()));
        }
        if (filtro.categoriaId() != null) {
            spec = spec.and(daCategoria(filtro.categoriaId()));
        }
        if (filtro.solicitanteId() != null) {
            spec = spec.and(doSolicitante(filtro.solicitanteId()));
        }
        if (filtro.tecnicoId() != null) {
            spec = spec.and(doTecnico(filtro.tecnicoId()));
        }
        if (filtro.dataInicio() != null) {
            spec = spec.and(abertoAPartirDe(filtro.dataInicio()));
        }
        if (filtro.dataFim() != null) {
            spec = spec.and(abertoAte(filtro.dataFim()));
        }
        return spec;
    }

    // GET /usuarios/{id}/chamados: abertos pelo usuário OU atribuídos a ele, com filtro opcional de status
    public static Specification<Chamado> doUsuario(Long usuarioId, StatusChamado status) {
        Specification<Chamado> spec = envolveUsuario(usuarioId);
        if (status != null) {
            spec = spec.and(doStatus(status));
        }
        return spec;
    }

    private static Specification<Chamado> doStatus(StatusChamado status) {
        return (root, query, cb) -> cb.equal(root.get("status"), status);
    }

    private static Specification<Chamado> daPrioridade(Prioridade prioridade) {
        return (root, query, cb) -> cb.equal(root.get("prioridade"), prioridade);
    }

    // Navega chamado.categoria.setor.id
    private static Specification<Chamado> doSetor(Long setorId) {
        return (root, query, cb) -> cb.equal(root.get("categoria").get("setor").get("id"), setorId);
    }

    private static Specification<Chamado> daCategoria(Long categoriaId) {
        return (root, query, cb) -> cb.equal(root.get("categoria").get("id"), categoriaId);
    }

    private static Specification<Chamado> doSolicitante(Long solicitanteId) {
        return (root, query, cb) -> cb.equal(root.get("solicitante").get("id"), solicitanteId);
    }

    private static Specification<Chamado> doTecnico(Long tecnicoId) {
        return (root, query, cb) -> cb.equal(root.get("tecnico").get("id"), tecnicoId);
    }

    // dataAbertura é LocalDateTime: o dia inicial começa às 00:00
    private static Specification<Chamado> abertoAPartirDe(LocalDate dataInicio) {
        return (root, query, cb) -> cb.greaterThanOrEqualTo(root.get("dataAbertura"), dataInicio.atStartOfDay());
    }

    // Inclui o dia final inteiro: dataAbertura < (dataFim + 1 dia) às 00:00
    private static Specification<Chamado> abertoAte(LocalDate dataFim) {
        return (root, query, cb) -> cb.lessThan(root.get("dataAbertura"), dataFim.plusDays(1).atStartOfDay());
    }

    // LEFT JOIN no técnico: chamados ainda sem técnico (ABERTO) também precisam aparecer para o solicitante
    private static Specification<Chamado> envolveUsuario(Long usuarioId) {
        return (root, query, cb) -> cb.or(
                cb.equal(root.get("solicitante").get("id"), usuarioId),
                cb.equal(root.join("tecnico", JoinType.LEFT).get("id"), usuarioId)
        );
    }
}
