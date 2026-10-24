package br.edu.ifrn.sistemachamados.dto;

import java.time.LocalDateTime;

// Formato padronizado de todos os erros da API, montado pelo GlobalExceptionHandler
public record ErroResponse(LocalDateTime timestamp, int status, String mensagem, String caminho) {
}
