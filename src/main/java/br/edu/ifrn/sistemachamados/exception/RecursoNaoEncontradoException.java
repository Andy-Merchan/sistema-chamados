package br.edu.ifrn.sistemachamados.exception;

// Recurso inexistente (será traduzida para HTTP 404)
public class RecursoNaoEncontradoException extends RuntimeException {

    public RecursoNaoEncontradoException(String mensagem) {
        super(mensagem);
    }
}
