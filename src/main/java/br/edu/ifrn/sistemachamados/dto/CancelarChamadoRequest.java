package br.edu.ifrn.sistemachamados.dto;

import jakarta.validation.constraints.NotNull;

public record CancelarChamadoRequest(
        @NotNull(message = "O solicitanteId é obrigatório")
        Long solicitanteId
) {
}
