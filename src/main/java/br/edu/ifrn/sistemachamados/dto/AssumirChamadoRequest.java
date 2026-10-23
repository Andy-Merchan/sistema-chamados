package br.edu.ifrn.sistemachamados.dto;

import jakarta.validation.constraints.NotNull;

public record AssumirChamadoRequest(
        @NotNull(message = "O tecnicoId é obrigatório")
        Long tecnicoId
) {
}
