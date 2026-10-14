package br.com.fiap.ecocycle.service;

import br.com.fiap.ecocycle.model.Usuario;
import br.com.fiap.ecocycle.repository.UsuarioRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Optional;

@Service
public class UsuarioService {

    @Autowired
    private UsuarioRepository repository;

    public List<Usuario> listarTodos() {
        return repository.findAll();
    }

    public Usuario salvar(Usuario usuario) {
        return repository.save(usuario);
    }

    public Optional<Usuario> buscarPorId(Long id) {
        return repository.findById(id);
    }

    public void excluir(Long id) {
        repository.deleteById(id);
    }

    public Usuario atualizar(Long id, Usuario novoUsuario) {
        return repository.findById(id).map(usuario -> {
            usuario.setNome(novoUsuario.getNome());
            usuario.setEmail(novoUsuario.getEmail());
            return repository.save(usuario);
        }).orElseThrow(() -> new RuntimeException("Usuário não encontrado"));
    }
}