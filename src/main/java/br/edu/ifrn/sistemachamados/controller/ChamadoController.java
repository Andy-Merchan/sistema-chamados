package br.edu.ifrn.sistemachamados.controller;

import br.edu.ifrn.sistemachamados.dto.AssumirChamadoRequest;
import br.edu.ifrn.sistemachamados.dto.CancelarChamadoRequest;
import br.edu.ifrn.sistemachamados.dto.ChamadoDetalheResponse;
import br.edu.ifrn.sistemachamados.dto.ChamadoFiltro;
import br.edu.ifrn.sistemachamados.dto.ChamadoRequest;
import br.edu.ifrn.sistemachamados.dto.ChamadoResponse;
import br.edu.ifrn.sistemachamados.dto.InteracaoRequest;
import br.edu.ifrn.sistemachamados.dto.InteracaoResponse;
import br.edu.ifrn.sistemachamados.dto.PaginaResponse;
import br.edu.ifrn.sistemachamados.dto.ResolverChamadoRequest;
import br.edu.ifrn.sistemachamados.service.ChamadoService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.web.PageableDefault;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.util.UriComponentsBuilder;

import java.net.URI;

// PATCH nas ações porque cada uma altera só parte do chamado (status e campos relacionados)
@RestController
@RequestMapping("/chamados")
@RequiredArgsConstructor
public class ChamadoController {

    private final ChamadoService chamadoService;

    @PostMapping
    public ResponseEntity<ChamadoResponse> abrir(@Valid @RequestBody ChamadoRequest request,
                                                 UriComponentsBuilder uriBuilder) {
        ChamadoResponse resposta = chamadoService.abrir(request);
        URI uri = uriBuilder.path("/chamados/{id}").buildAndExpand(resposta.id()).toUri();
        return ResponseEntity.created(uri).body(resposta);
    }

    // Ex.: GET /chamados?status=ABERTO&setorId=1&prioridade=ALTA&page=0&size=10&sort=dataAbertura,desc
    // @Valid ativa a validação do período (dataInicio <= dataFim) declarada no ChamadoFiltro
    @GetMapping
    public PaginaResponse<ChamadoResponse> listar(@Valid ChamadoFiltro filtro,
                                                  @PageableDefault(sort = "dataAbertura", direction = Sort.Direction.DESC)
                                                  Pageable pageable) {
        return chamadoService.listar(filtro, pageable);
    }

    @GetMapping("/{id}")
    public ChamadoDetalheResponse buscarPorId(@PathVariable Long id) {
        return chamadoService.buscarPorId(id);
    }

    @PatchMapping("/{id}/assumir")
    public ChamadoResponse assumir(@PathVariable Long id, @Valid @RequestBody AssumirChamadoRequest request) {
        return chamadoService.assumir(id, request);
    }

    @PatchMapping("/{id}/resolver")
    public ChamadoResponse resolver(@PathVariable Long id, @Valid @RequestBody ResolverChamadoRequest request) {
        return chamadoService.resolver(id, request);
    }

    @PatchMapping("/{id}/cancelar")
    public ChamadoResponse cancelar(@PathVariable Long id, @Valid @RequestBody CancelarChamadoRequest request) {
        return chamadoService.cancelar(id, request);
    }

    // A interação não tem rota própria de consulta (aparece em GET /chamados/{id}), por isso sem Location
    @PostMapping("/{id}/interacoes")
    public ResponseEntity<InteracaoResponse> registrarInteracao(@PathVariable Long id,
                                                                @Valid @RequestBody InteracaoRequest request) {
        return ResponseEntity.status(HttpStatus.CREATED).body(chamadoService.registrarInteracao(id, request));
    }
}
