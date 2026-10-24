package br.edu.ifrn.sistemachamados.repository;

import br.edu.ifrn.sistemachamados.model.Categoria;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;

public interface CategoriaRepository extends JpaRepository<Categoria, Long>, JpaSpecificationExecutor<Categoria> {

    // "SetorId" navega pela relação: categoria.setor.id
    Page<Categoria> findBySetorId(Long setorId, Pageable pageable);

    boolean existsBySetorId(Long setorId);
}
