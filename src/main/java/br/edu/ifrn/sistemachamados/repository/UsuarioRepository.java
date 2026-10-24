package br.edu.ifrn.sistemachamados.repository;

import br.edu.ifrn.sistemachamados.model.Usuario;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;

public interface UsuarioRepository extends JpaRepository<Usuario, Long>, JpaSpecificationExecutor<Usuario> {

    boolean existsByEmail(String email);

    // Usado na atualização: procura o email em outro usuário que não seja o próprio
    boolean existsByEmailAndIdNot(String email, Long id);

    boolean existsBySetorId(Long setorId);
}
