package br.edu.ifrn.sistemachamados.dto;

import br.edu.ifrn.sistemachamados.model.Interacao;

import java.time.LocalDateTime;

public record InteracaoResponse(Long id, String texto, LocalDateTime dataHora, Long autorId, String autorNome) {

    public static InteracaoResponse de(Interacao interacao) {
        return new InteracaoResponse(
                interacao.getId(),
                interacao.getTexto(),
                interacao.getDataHora(),
                interacao.getAutor().getId(),
                interacao.getAutor().getNome()
        );
    }
}
