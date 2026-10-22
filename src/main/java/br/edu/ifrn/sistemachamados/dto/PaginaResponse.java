package br.edu.ifrn.sistemachamados.dto;

import org.springframework.data.domain.Page;

import java.util.List;

// Formato fixo das listagens: itens + metadados da página
public record PaginaResponse<T>(
        List<T> itens,
        int pagina,
        int tamanho,
        long totalElementos,
        int totalPaginas
) {

    public static <T> PaginaResponse<T> de(Page<T> page) {
        return new PaginaResponse<>(
                page.getContent(),
                page.getNumber(),
                page.getSize(),
                page.getTotalElements(),
                page.getTotalPages()
        );
    }
}
