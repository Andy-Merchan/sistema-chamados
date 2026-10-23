package br.edu.ifrn.sistemachamados.dto;

import br.edu.ifrn.sistemachamados.model.Chamado;
import br.edu.ifrn.sistemachamados.model.Usuario;
import br.edu.ifrn.sistemachamados.model.enums.Prioridade;
import br.edu.ifrn.sistemachamados.model.enums.StatusChamado;

import java.time.LocalDateTime;

// Resumo usado nas listagens e nas ações (assumir, resolver, cancelar): sem as interações
public record ChamadoResponse(
        Long id,
        String titulo,
        String descricao,
        String local,
        Prioridade prioridade,
        StatusChamado status,
        LocalDateTime dataAbertura,
        LocalDateTime dataFechamento,
        String solucao,
        Long solicitanteId,
        String solicitanteNome,
        Long categoriaId,
        String categoriaNome,
        String setorSigla,
        Long tecnicoId,
        String tecnicoNome
) {

    public static ChamadoResponse de(Chamado chamado) {
        // O técnico só existe depois que o chamado é assumido
        Usuario tecnico = chamado.getTecnico();
        return new ChamadoResponse(
                chamado.getId(),
                chamado.getTitulo(),
                chamado.getDescricao(),
                chamado.getLocal(),
                chamado.getPrioridade(),
                chamado.getStatus(),
                chamado.getDataAbertura(),
                chamado.getDataFechamento(),
                chamado.getSolucao(),
                chamado.getSolicitante().getId(),
                chamado.getSolicitante().getNome(),
                chamado.getCategoria().getId(),
                chamado.getCategoria().getNome(),
                chamado.getCategoria().getSetor().getSigla(),
                tecnico != null ? tecnico.getId() : null,
                tecnico != null ? tecnico.getNome() : null
        );
    }
}
