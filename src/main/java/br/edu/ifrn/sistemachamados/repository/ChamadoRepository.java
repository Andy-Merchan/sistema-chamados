package br.edu.ifrn.sistemachamados.repository;

import br.edu.ifrn.sistemachamados.model.Chamado;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;

public interface ChamadoRepository extends JpaRepository<Chamado, Long>, JpaSpecificationExecutor<Chamado> {

    // Regra 8: não excluir categoria ou setor com chamados vinculados
    boolean existsByCategoriaId(Long categoriaId);

    // Navega chamado.categoria.setor.id
    boolean existsByCategoriaSetorId(Long setorId);
}
