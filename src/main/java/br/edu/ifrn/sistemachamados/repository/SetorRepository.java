package br.edu.ifrn.sistemachamados.repository;

import br.edu.ifrn.sistemachamados.model.Setor;
import org.springframework.data.jpa.repository.JpaRepository;

public interface SetorRepository extends JpaRepository<Setor, Long> {

    boolean existsBySigla(String sigla);

    // Usado na atualização: procura a sigla em outro setor que não seja o próprio
    boolean existsBySiglaAndIdNot(String sigla, Long id);
}
