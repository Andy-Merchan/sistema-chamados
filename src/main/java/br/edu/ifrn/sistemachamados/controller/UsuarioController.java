package br.edu.ifrn.sistemachamados.controller;

import br.edu.ifrn.sistemachamados.dto.ChamadoResponse;
import br.edu.ifrn.sistemachamados.dto.PaginaResponse;
import br.edu.ifrn.sistemachamados.dto.UsuarioRequest;
import br.edu.ifrn.sistemachamados.dto.UsuarioResponse;
import br.edu.ifrn.sistemachamados.service.ChamadoService;
import br.edu.ifrn.sistemachamados.service.UsuarioService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Pageable;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.util.UriComponentsBuilder;

import java.net.URI;

// Sem DELETE: o enunciado pede apenas POST, GET e PUT para usuários
@RestController
@RequestMapping("/usuarios")
@RequiredArgsConstructor
public class UsuarioController {

    private final UsuarioService usuarioService;
    private final ChamadoService chamadoService;

    @PostMapping
    public ResponseEntity<UsuarioResponse> criar(@Valid @RequestBody UsuarioRequest request,
                                                 UriComponentsBuilder uriBuilder) {
        UsuarioResponse resposta = usuarioService.criar(request);
        URI uri = uriBuilder.path("/usuarios/{id}").buildAndExpand(resposta.id()).toUri();
        return ResponseEntity.created(uri).body(resposta);
    }

    @GetMapping
    public PaginaResponse<UsuarioResponse> listar(Pageable pageable) {
        return usuarioService.listar(pageable);
    }

    @GetMapping("/{id}")
    public UsuarioResponse buscarPorId(@PathVariable Long id) {
        return usuarioService.buscarPorId(id);
    }

    // Chamados abertos pelo usuário (solicitante) ou atribuídos a ele (técnico)
    @GetMapping("/{id}/chamados")
    public PaginaResponse<ChamadoResponse> listarChamados(@PathVariable Long id, Pageable pageable) {
        return chamadoService.listarPorUsuario(id, pageable);
    }

    @PutMapping("/{id}")
    public UsuarioResponse atualizar(@PathVariable Long id, @Valid @RequestBody UsuarioRequest request) {
        return usuarioService.atualizar(id, request);
    }
}
