package br.edu.ifrn.sistemachamados.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

public record InteracaoRequest(
        @NotNull(message = "O autorId é obrigatório")
        Long autorId,

        @NotBlank(message = "O texto é obrigatório")
        @Size(max = 2000, message = "O texto deve ter no máximo 2000 caracteres")
        String texto
) {
}
