package br.edu.ifrn.sistemachamados.controller;

import br.edu.ifrn.sistemachamados.dto.CategoriaResponse;
import br.edu.ifrn.sistemachamados.dto.PaginaResponse;
import br.edu.ifrn.sistemachamados.dto.SetorFiltro;
import br.edu.ifrn.sistemachamados.dto.SetorRequest;
import br.edu.ifrn.sistemachamados.dto.SetorResponse;
import br.edu.ifrn.sistemachamados.service.CategoriaService;
import br.edu.ifrn.sistemachamados.service.SetorService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Pageable;
import org.springframework.data.web.PageableDefault;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.util.UriComponentsBuilder;

import java.net.URI;

@RestController
@RequestMapping("/setores")
@RequiredArgsConstructor
public class SetorController {

    private final SetorService setorService;
    private final CategoriaService categoriaService;

    @PostMapping
    public ResponseEntity<SetorResponse> criar(@Valid @RequestBody SetorRequest request,
                                               UriComponentsBuilder uriBuilder) {
        SetorResponse resposta = setorService.criar(request);
        URI uri = uriBuilder.path("/setores/{id}").buildAndExpand(resposta.id()).toUri();
        return ResponseEntity.created(uri).body(resposta);
    }

    // Os filtros chegam pela query string e o Spring os agrupa no record. Ex.: GET /setores?nome=tec
    @GetMapping
    // Sem ?sort= na URL, ordena por nome para a resposta ter ordem estável
    public PaginaResponse<SetorResponse> listar(SetorFiltro filtro,
                                                @PageableDefault(sort = "nome") Pageable pageable) {
        return setorService.listar(filtro, pageable);
    }

    @GetMapping("/{id}")
    public SetorResponse buscarPorId(@PathVariable Long id) {
        return setorService.buscarPorId(id);
    }

    @GetMapping("/{id}/categorias")
    public PaginaResponse<CategoriaResponse> listarCategorias(@PathVariable Long id,
                                                              @PageableDefault(sort = "nome") Pageable pageable) {
        return categoriaService.listarPorSetor(id, pageable);
    }

    @PutMapping("/{id}")
    public SetorResponse atualizar(@PathVariable Long id, @Valid @RequestBody SetorRequest request) {
        return setorService.atualizar(id, request);
    }

    @DeleteMapping("/{id}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void excluir(@PathVariable Long id) {
        setorService.excluir(id);
    }
}
