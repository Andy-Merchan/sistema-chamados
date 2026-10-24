package br.edu.ifrn.sistemachamados.dto;

import br.edu.ifrn.sistemachamados.model.enums.TipoUsuario;
import jakarta.validation.constraints.AssertTrue;
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

        // Obrigatório apenas para TECNICO (validado em isSetorCoerenteComTipo)
        Long setorId
) {

    // Validação entre dois campos: técnico pertence a um setor, solicitante não.
    // Dado inválido da própria requisição, por isso 400 (via @Valid) e não 409
    @AssertTrue(message = "O setorId é obrigatório para TECNICO e não deve ser informado para SOLICITANTE")
    public boolean isSetorCoerenteComTipo() {
        return tipo == null || (tipo == TipoUsuario.TECNICO) == (setorId != null);
    }
}
