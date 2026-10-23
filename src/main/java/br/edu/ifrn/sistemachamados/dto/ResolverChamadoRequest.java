package br.edu.ifrn.sistemachamados.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

public record ResolverChamadoRequest(
        @NotNull(message = "O tecnicoId é obrigatório")
        Long tecnicoId,

        // Regra 4: a solução é obrigatória
        @NotBlank(message = "A solução é obrigatória")
        @Size(max = 2000, message = "A solução deve ter no máximo 2000 caracteres")
        String solucao
) {
}
