package br.com.fiap.ecocycle.service;

import br.com.fiap.ecocycle.model.PontoDeColeta;
import br.com.fiap.ecocycle.repository.PontoDeColetaRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Optional;

@Service
public class PontoDeColetaService {

    @Autowired
    private PontoDeColetaRepository repository;

    public List<PontoDeColeta> listarTodos() {
        return repository.findAll();
    }

    public PontoDeColeta salvar(PontoDeColeta ponto) {
        return repository.save(ponto);
    }

    public Optional<PontoDeColeta> buscarPorId(Long id) {
        return repository.findById(id);
    }

    public void excluir(Long id) {
        repository.deleteById(id);
    }

    public PontoDeColeta atualizar(Long id, PontoDeColeta novoPonto) {
        return repository.findById(id).map(ponto -> {
            ponto.setNome(novoPonto.getNome());
            ponto.setEndereco(novoPonto.getEndereco());
            ponto.setTipoResiduo(novoPonto.getTipoResiduo());
            return repository.save(ponto);
        }).orElseThrow(() -> new RuntimeException("Ponto de coleta não encontrado"));
    }
}