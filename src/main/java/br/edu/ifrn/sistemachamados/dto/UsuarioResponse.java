package br.edu.ifrn.sistemachamados.dto;

import br.edu.ifrn.sistemachamados.model.Setor;
import br.edu.ifrn.sistemachamados.model.Usuario;
import br.edu.ifrn.sistemachamados.model.enums.TipoUsuario;

public record UsuarioResponse(
        Long id,
        String nome,
        String email,
        String matricula,
        TipoUsuario tipo,
        Long setorId,
        String setorSigla
) {

    public static UsuarioResponse de(Usuario usuario) {
        // Solicitantes não têm setor
        Setor setor = usuario.getSetor();
        return new UsuarioResponse(
                usuario.getId(),
                usuario.getNome(),
                usuario.getEmail(),
                usuario.getMatricula(),
                usuario.getTipo(),
                setor != null ? setor.getId() : null,
                setor != null ? setor.getSigla() : null
        );
    }
}
