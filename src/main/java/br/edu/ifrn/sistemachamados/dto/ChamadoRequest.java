package br.edu.ifrn.sistemachamados.dto;

import br.edu.ifrn.sistemachamados.model.enums.Prioridade;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

public record ChamadoRequest(
        @NotBlank(message = "O título é obrigatório")
        @Size(max = 150, message = "O título deve ter no máximo 150 caracteres")
        String titulo,

        @NotBlank(message = "A descrição é obrigatória")
        @Size(max = 2000, message = "A descrição deve ter no máximo 2000 caracteres")
        String descricao,

        @NotBlank(message = "O local é obrigatório")
        @Size(max = 150, message = "O local deve ter no máximo 150 caracteres")
        String local,

        // Opcional: quando não informada, o Service usa MEDIA (regra 1)
        Prioridade prioridade,

        @NotNull(message = "O categoriaId é obrigatório")
        Long categoriaId,

        @NotNull(message = "O solicitanteId é obrigatório")
        Long solicitanteId
) {
}
