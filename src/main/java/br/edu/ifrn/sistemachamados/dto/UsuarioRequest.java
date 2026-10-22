package br.edu.ifrn.sistemachamados.dto;

import br.edu.ifrn.sistemachamados.model.enums.TipoUsuario;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

public record UsuarioRequest(
        @NotBlank(message = "O nome é obrigatório")
        @Size(max = 100, message = "O nome deve ter no máximo 100 caracteres")
        String nome,

        @NotBlank(message = "O email é obrigatório")
        @Email(message = "O email deve ser válido")
        @Size(max = 150, message = "O email deve ter no máximo 150 caracteres")
        String email,

        @NotBlank(message = "A matrícula é obrigatória")
        @Size(max = 20, message = "A matrícula deve ter no máximo 20 caracteres")
        String matricula,

        @NotNull(message = "O tipo é obrigatório")
        TipoUsuario tipo,

        // Obrigatório apenas para TECNICO (validado no Service)
        Long setorId
) {
}
