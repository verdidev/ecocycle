package br.com.fiap.ecocycle.service;

import br.com.fiap.ecocycle.model.Material;
import br.com.fiap.ecocycle.repository.MaterialRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Optional;

@Service
public class MaterialService {

    @Autowired
    private MaterialRepository repository;

    public List<Material> listarTodos() {
        return repository.findAll();
    }

    public Material salvar(Material material) {
        return repository.save(material);
    }

    public Optional<Material> buscarPorId(Long id) {
        return repository.findById(id);
    }

    public void excluir(Long id) {
        repository.deleteById(id);
    }

    public Material atualizar(Long id, Material novoMaterial) {
        return repository.findById(id).map(material -> {
            material.setNome(novoMaterial.getNome());
            material.setPeso(novoMaterial.getPeso());
            material.setTipo(novoMaterial.getTipo());
            return repository.save(material);
        }).orElseThrow(() -> new RuntimeException("Material não encontrado"));
    }
}