package br.edu.ifrn.sistemachamados.dto;

import br.edu.ifrn.sistemachamados.model.Chamado;
import br.edu.ifrn.sistemachamados.model.Usuario;
import br.edu.ifrn.sistemachamados.model.enums.Prioridade;
import br.edu.ifrn.sistemachamados.model.enums.StatusChamado;

import java.time.LocalDateTime;
import java.util.List;

// Usado em GET /chamados/{id}: os mesmos dados do resumo + o histórico de interações
public record ChamadoDetalheResponse(
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
        String tecnicoNome,
        List<InteracaoResponse> interacoes
) {

    public static ChamadoDetalheResponse de(Chamado chamado) {
        Usuario tecnico = chamado.getTecnico();
        return new ChamadoDetalheResponse(
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
                tecnico != null ? tecnico.getNome() : null,
                chamado.getInteracoes().stream().map(InteracaoResponse::de).toList()
        );
    }
}
