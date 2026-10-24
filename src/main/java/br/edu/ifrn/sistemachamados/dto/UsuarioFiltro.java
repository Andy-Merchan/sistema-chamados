package br.edu.ifrn.sistemachamados.dto;

import br.edu.ifrn.sistemachamados.model.enums.TipoUsuario;

// Filtros opcionais de GET /usuarios (ex.: ?nome=andy&tipo=TECNICO&setorId=1)
public record UsuarioFiltro(String nome, TipoUsuario tipo, Long setorId) {
}
