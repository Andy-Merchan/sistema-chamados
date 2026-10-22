package br.edu.ifrn.sistemachamados.dto;

import br.edu.ifrn.sistemachamados.model.Setor;

public record SetorResponse(Long id, String nome, String sigla) {

    public static SetorResponse de(Setor setor) {
        return new SetorResponse(setor.getId(), setor.getNome(), setor.getSigla());
    }
}
