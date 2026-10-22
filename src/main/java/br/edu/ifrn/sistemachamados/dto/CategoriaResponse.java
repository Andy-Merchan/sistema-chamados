package br.edu.ifrn.sistemachamados.dto;

import br.edu.ifrn.sistemachamados.model.Categoria;

public record CategoriaResponse(Long id, String nome, String descricao, Long setorId, String setorNome) {

    public static CategoriaResponse de(Categoria categoria) {
        return new CategoriaResponse(
                categoria.getId(),
                categoria.getNome(),
                categoria.getDescricao(),
                categoria.getSetor().getId(),
                categoria.getSetor().getNome()
        );
    }
}
