package br.edu.ifrn.sistemachamados.exception;

import br.edu.ifrn.sistemachamados.dto.ErroResponse;
import jakarta.servlet.http.HttpServletRequest;
import lombok.extern.slf4j.Slf4j;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.data.core.PropertyReferenceException;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.http.converter.HttpMessageNotReadableException;
import org.springframework.validation.FieldError;
import org.springframework.web.HttpRequestMethodNotSupportedException;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.web.method.annotation.MethodArgumentTypeMismatchException;
import org.springframework.web.servlet.resource.NoResourceFoundException;

import java.time.LocalDateTime;
import java.util.stream.Collectors;

// Intercepta as exceções lançadas por qualquer Controller e as traduz no JSON padrão de erro
// (timestamp, status, mensagem, caminho). Assim nenhum Controller precisa de try/catch.
@Slf4j
@RestControllerAdvice
public class GlobalExceptionHandler {

    // 404: recurso pedido pelo id não existe
    @ExceptionHandler(RecursoNaoEncontradoException.class)
    public ResponseEntity<ErroResponse> tratarNaoEncontrado(RecursoNaoEncontradoException ex,
                                                            HttpServletRequest request) {
        return responder(HttpStatus.NOT_FOUND, ex.getMessage(), request);
    }

    // 409: a requisição é válida, mas conflita com uma regra de negócio ou com o estado atual
    @ExceptionHandler(RegraNegocioException.class)
    public ResponseEntity<ErroResponse> tratarRegraNegocio(RegraNegocioException ex, HttpServletRequest request) {
        return responder(HttpStatus.CONFLICT, ex.getMessage(), request);
    }

    // 400: falha do @Valid (no corpo JSON ou nos filtros da query string).
    // Junta as mensagens de todos os campos inválidos numa só mensagem
    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ResponseEntity<ErroResponse> tratarValidacao(MethodArgumentNotValidException ex,
                                                        HttpServletRequest request) {
        String mensagem = ex.getBindingResult().getAllErrors().stream()
                .map(erro -> erro instanceof FieldError campo ? descreverCampo(campo) : erro.getDefaultMessage())
                .sorted()
                .collect(Collectors.joining("; "));
        return responder(HttpStatus.BAD_REQUEST, mensagem, request);
    }

    // 400: JSON mal formatado ou valor que não pode ser convertido (ex.: "prioridade": "URGENTE")
    @ExceptionHandler(HttpMessageNotReadableException.class)
    public ResponseEntity<ErroResponse> tratarCorpoInvalido(HttpMessageNotReadableException ex,
                                                            HttpServletRequest request) {
        return responder(HttpStatus.BAD_REQUEST,
                "Corpo da requisição inválido: verifique o formato do JSON e os valores informados", request);
    }

    // 400: parâmetro de rota ou de query com tipo errado (ex.: /chamados/abc ou ?status=XYZ)
    @ExceptionHandler(MethodArgumentTypeMismatchException.class)
    public ResponseEntity<ErroResponse> tratarTipoInvalido(MethodArgumentTypeMismatchException ex,
                                                           HttpServletRequest request) {
        String mensagem = "Valor inválido '" + ex.getValue() + "' para o parâmetro '" + ex.getName() + "'";
        return responder(HttpStatus.BAD_REQUEST, mensagem, request);
    }

    // 400: ?sort= com um atributo que não existe na entidade (ex.: ?sort=xyz,asc)
    @ExceptionHandler(PropertyReferenceException.class)
    public ResponseEntity<ErroResponse> tratarOrdenacaoInvalida(PropertyReferenceException ex,
                                                                HttpServletRequest request) {
        return responder(HttpStatus.BAD_REQUEST,
                "Propriedade de ordenação inválida: " + ex.getPropertyName(), request);
    }

    // 409: o banco recusou a operação por uma restrição (ex.: email único gravado ao mesmo tempo
    // por duas requisições, passando pela verificação do Service)
    @ExceptionHandler(DataIntegrityViolationException.class)
    public ResponseEntity<ErroResponse> tratarIntegridade(DataIntegrityViolationException ex,
                                                          HttpServletRequest request) {
        return responder(HttpStatus.CONFLICT,
                "A operação viola uma restrição de integridade dos dados", request);
    }

    // 404: rota que não existe na API
    @ExceptionHandler(NoResourceFoundException.class)
    public ResponseEntity<ErroResponse> tratarRotaInexistente(NoResourceFoundException ex,
                                                              HttpServletRequest request) {
        return responder(HttpStatus.NOT_FOUND, "Rota não encontrada", request);
    }

    // 405: a rota existe, mas não com esse método HTTP (ex.: DELETE /usuarios/1)
    @ExceptionHandler(HttpRequestMethodNotSupportedException.class)
    public ResponseEntity<ErroResponse> tratarMetodoNaoSuportado(HttpRequestMethodNotSupportedException ex,
                                                                 HttpServletRequest request) {
        return responder(HttpStatus.METHOD_NOT_ALLOWED,
                "Método " + ex.getMethod() + " não suportado nesta rota", request);
    }

    // 500: qualquer erro não previsto. O detalhe vai para o log, nunca para o cliente
    @ExceptionHandler(Exception.class)
    public ResponseEntity<ErroResponse> tratarErroInesperado(Exception ex, HttpServletRequest request) {
        log.error("Erro inesperado em {}", request.getRequestURI(), ex);
        return responder(HttpStatus.INTERNAL_SERVER_ERROR, "Erro interno no servidor", request);
    }

    // Erro de conversão (ex.: tipo=ADMIN) gera uma mensagem técnica em inglês; trocamos por uma legível
    private String descreverCampo(FieldError campo) {
        if (campo.isBindingFailure()) {
            return campo.getField() + ": valor inválido '" + campo.getRejectedValue() + "'";
        }
        return campo.getField() + ": " + campo.getDefaultMessage();
    }

    private ResponseEntity<ErroResponse> responder(HttpStatus status, String mensagem, HttpServletRequest request) {
        ErroResponse corpo = new ErroResponse(LocalDateTime.now(), status.value(), mensagem, request.getRequestURI());
        return ResponseEntity.status(status).body(corpo);
    }
}
