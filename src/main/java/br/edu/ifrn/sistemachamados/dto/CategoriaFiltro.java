package br.edu.ifrn.sistemachamados.dto;

// Filtros opcionais de GET /categorias (ex.: ?nome=rede&setorId=1)
public record CategoriaFiltro(String nome, Long setorId) {
}
