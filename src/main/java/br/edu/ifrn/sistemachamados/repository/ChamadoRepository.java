package br.edu.ifrn.sistemachamados.repository;

import br.edu.ifrn.sistemachamados.model.Chamado;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;

public interface ChamadoRepository extends JpaRepository<Chamado, Long> {

    // Regra 8: não excluir categoria ou setor com chamados vinculados
    boolean existsByCategoriaId(Long categoriaId);

    // Navega chamado.categoria.setor.id
    boolean existsByCategoriaSetorId(Long setorId);

    // GET /usuarios/{id}/chamados: chamados abertos pelo usuário ou atribuídos a ele
    Page<Chamado> findBySolicitanteIdOrTecnicoId(Long solicitanteId, Long tecnicoId, Pageable pageable);
}
