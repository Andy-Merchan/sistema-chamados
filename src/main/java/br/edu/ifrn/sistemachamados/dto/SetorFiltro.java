package br.edu.ifrn.sistemachamados.dto;

// Filtros opcionais de GET /setores, lidos da query string (ex.: ?nome=tec)
public record SetorFiltro(String nome) {
}
