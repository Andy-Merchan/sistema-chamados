package br.edu.ifrn.sistemachamados.exception;

// Violação de regra de negócio (será traduzida para HTTP 409)
public class RegraNegocioException extends RuntimeException {

    public RegraNegocioException(String mensagem) {
        super(mensagem);
    }
}
