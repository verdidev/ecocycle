package br.com.fiap.ecocycle.service;

import br.com.fiap.ecocycle.model.Usuario;
import br.com.fiap.ecocycle.repository.UsuarioRepository;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

/**
 * Testes unitarios do UsuarioService (sem Spring e sem banco).
 */
@ExtendWith(MockitoExtension.class)
class UsuarioServiceTest {

    @Mock
    private UsuarioRepository repository;

    @InjectMocks
    private UsuarioService service;

    private static Usuario usuario(Long id, String nome, String email) {
        Usuario u = new Usuario();
        u.setId(id);
        u.setNome(nome);
        u.setEmail(email);
        return u;
    }

    @Test
    void listarTodosDeveRetornarOsUsuariosDoRepositorio() {
        when(repository.findAll()).thenReturn(List.of(usuario(1L, "Ana", "ana@email.com")));

        assertThat(service.listarTodos()).hasSize(1)
                .first().extracting(Usuario::getNome).isEqualTo("Ana");
    }

    @Test
    void atualizarDeveAlterarNomeEEmailDoUsuarioExistente() {
        Usuario existente = usuario(1L, "Ana", "ana@email.com");
        when(repository.findById(1L)).thenReturn(Optional.of(existente));
        when(repository.save(any(Usuario.class))).thenAnswer(inv -> inv.getArgument(0));

        Usuario atualizado = service.atualizar(1L, usuario(null, "Ana Souza", "ana.souza@email.com"));

        assertThat(atualizado.getId()).isEqualTo(1L);
        assertThat(atualizado.getNome()).isEqualTo("Ana Souza");
        assertThat(atualizado.getEmail()).isEqualTo("ana.souza@email.com");
        verify(repository).save(existente);
    }

    @Test
    void atualizarDeveLancarExcecaoQuandoUsuarioNaoExiste() {
        when(repository.findById(99L)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> service.atualizar(99L, usuario(null, "X", "x@email.com")))
                .isInstanceOf(RuntimeException.class)
                .hasMessageContaining("Usuário não encontrado");
        verify(repository, never()).save(any());
    }

    @Test
    void excluirDeveDelegarAoRepositorio() {
        service.excluir(5L);

        verify(repository).deleteById(5L);
    }
}
