package br.edu.ifrn.sistemachamados.dto;

import br.edu.ifrn.sistemachamados.model.enums.Prioridade;
import br.edu.ifrn.sistemachamados.model.enums.StatusChamado;
import jakarta.validation.constraints.AssertTrue;
import org.springframework.format.annotation.DateTimeFormat;

import java.time.LocalDate;

// Filtros opcionais de GET /chamados, combináveis entre si; filtro nulo é ignorado.
// Datas no formato ISO (ex.: ?dataInicio=2026-10-01&dataFim=2026-10-31), ambas inclusivas
public record ChamadoFiltro(
        StatusChamado status,
        Prioridade prioridade,
        Long setorId,
        Long categoriaId,
        Long solicitanteId,
        Long tecnicoId,

        @DateTimeFormat(iso = DateTimeFormat.ISO.DATE)
        LocalDate dataInicio,

        @DateTimeFormat(iso = DateTimeFormat.ISO.DATE)
        LocalDate dataFim
) {

    // Validação entre dois campos: o Bean Validation chama este método por causa do @Valid no Controller
    @AssertTrue(message = "A dataInicio não pode ser posterior à dataFim")
    public boolean isPeriodoValido() {
        return dataInicio == null || dataFim == null || !dataInicio.isAfter(dataFim);
    }
}
