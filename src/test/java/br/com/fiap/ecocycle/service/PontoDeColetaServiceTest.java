package br.com.fiap.ecocycle.service;

import br.com.fiap.ecocycle.model.PontoDeColeta;
import br.com.fiap.ecocycle.repository.PontoDeColetaRepository;
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
 * Testes unitarios do PontoDeColetaService (sem Spring e sem banco).
 */
@ExtendWith(MockitoExtension.class)
class PontoDeColetaServiceTest {

    @Mock
    private PontoDeColetaRepository repository;

    @InjectMocks
    private PontoDeColetaService service;

    private static PontoDeColeta ponto(Long id, String nome, String endereco, String tipoResiduo) {
        PontoDeColeta p = new PontoDeColeta();
        p.setId(id);
        p.setNome(nome);
        p.setEndereco(endereco);
        p.setTipoResiduo(tipoResiduo);
        return p;
    }

    @Test
    void atualizarDeveAlterarTodosOsCamposInclusiveTipoResiduo() {
        PontoDeColeta existente = ponto(1L, "Ecoponto Centro", "Rua A, 10", "Vidro");
        when(repository.findById(1L)).thenReturn(Optional.of(existente));
        when(repository.save(any(PontoDeColeta.class))).thenAnswer(inv -> inv.getArgument(0));

        PontoDeColeta atualizado = service.atualizar(1L, ponto(null, "Ecoponto Sul", "Av. B, 200", "Eletronicos"));

        assertThat(atualizado.getNome()).isEqualTo("Ecoponto Sul");
        assertThat(atualizado.getEndereco()).isEqualTo("Av. B, 200");
        assertThat(atualizado.getTipoResiduo()).isEqualTo("Eletronicos");
    }

    @Test
    void atualizarDeveLancarExcecaoQuandoPontoNaoExiste() {
        when(repository.findById(3L)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> service.atualizar(3L, ponto(null, "X", "Y", "Z")))
                .isInstanceOf(RuntimeException.class)
                .hasMessageContaining("Ponto de coleta não encontrado");
        verify(repository, never()).save(any());
    }
}
