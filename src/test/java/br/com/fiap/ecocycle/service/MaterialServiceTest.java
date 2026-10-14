package br.com.fiap.ecocycle.service;

import br.com.fiap.ecocycle.model.Material;
import br.com.fiap.ecocycle.repository.MaterialRepository;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

/**
 * Testes unitarios do MaterialService (sem Spring e sem banco).
 */
@ExtendWith(MockitoExtension.class)
class MaterialServiceTest {

    @Mock
    private MaterialRepository repository;

    @InjectMocks
    private MaterialService service;

    private static Material material(Long id, String nome, String tipo, Double peso) {
        Material m = new Material();
        m.setId(id);
        m.setNome(nome);
        m.setTipo(tipo);
        m.setPeso(peso);
        return m;
    }

    @Test
    void salvarDeveRetornarOMaterialPersistido() {
        Material novo = material(null, "Garrafa PET", "Plastico", 0.5);
        when(repository.save(novo)).thenReturn(material(10L, "Garrafa PET", "Plastico", 0.5));

        Material salvo = service.salvar(novo);

        assertThat(salvo.getId()).isEqualTo(10L);
    }

    @Test
    void buscarPorIdDeveRetornarVazioQuandoNaoExiste() {
        when(repository.findById(42L)).thenReturn(Optional.empty());

        assertThat(service.buscarPorId(42L)).isEmpty();
    }

    @Test
    void atualizarDeveAlterarNomePesoETipo() {
        Material existente = material(1L, "Lata", "Metal", 0.1);
        when(repository.findById(1L)).thenReturn(Optional.of(existente));
        when(repository.save(any(Material.class))).thenAnswer(inv -> inv.getArgument(0));

        Material atualizado = service.atualizar(1L, material(null, "Lata de aluminio", "Aluminio", 0.25));

        assertThat(atualizado.getNome()).isEqualTo("Lata de aluminio");
        assertThat(atualizado.getTipo()).isEqualTo("Aluminio");
        assertThat(atualizado.getPeso()).isEqualTo(0.25);
    }

    @Test
    void atualizarDeveLancarExcecaoQuandoMaterialNaoExiste() {
        when(repository.findById(7L)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> service.atualizar(7L, material(null, "X", "Y", 1.0)))
                .isInstanceOf(RuntimeException.class)
                .hasMessageContaining("Material não encontrado");
        verify(repository, never()).save(any());
    }
}
