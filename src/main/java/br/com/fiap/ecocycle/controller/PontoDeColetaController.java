package br.com.fiap.ecocycle.controller;

import br.com.fiap.ecocycle.model.PontoDeColeta;
import br.com.fiap.ecocycle.service.PontoDeColetaService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/pontos")
public class PontoDeColetaController {

    @Autowired
    private PontoDeColetaService service;

    @GetMapping
    public List<PontoDeColeta> listar() {
        return service.listarTodos();
    }

    @GetMapping("/{id}")
    public ResponseEntity<PontoDeColeta> buscar(@PathVariable Long id) {
        return service.buscarPorId(id)
                .map(ResponseEntity::ok)
                .orElse(ResponseEntity.notFound().build());
    }

    @PostMapping
    public ResponseEntity<PontoDeColeta> criar(@RequestBody PontoDeColeta ponto) {
        return ResponseEntity.status(201).body(service.salvar(ponto));
    }

    @PutMapping("/{id}")
    public ResponseEntity<PontoDeColeta> atualizar(@PathVariable Long id, @RequestBody PontoDeColeta ponto) {
        try {
            return ResponseEntity.ok(service.atualizar(id, ponto));
        } catch (RuntimeException e) {
            return ResponseEntity.notFound().build();
        }
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deletar(@PathVariable Long id) {
        service.excluir(id);
        return ResponseEntity.noContent().build();
    }
}