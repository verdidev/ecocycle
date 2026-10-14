package br.com.fiap.ecocycle.controller;

import br.com.fiap.ecocycle.model.PontoDeColeta;
import br.com.fiap.ecocycle.service.PontoDeColetaService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;

import java.util.List;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

/**
 * Testes da camada HTTP do PontoDeColetaController com MockMvc standalone.
 */
@ExtendWith(MockitoExtension.class)
class PontoDeColetaControllerTest {

    @Mock
    private PontoDeColetaService service;

    @InjectMocks
    private PontoDeColetaController controller;

    private MockMvc mockMvc;

    @BeforeEach
    void setUp() {
        mockMvc = MockMvcBuilders.standaloneSetup(controller).build();
    }

    private static PontoDeColeta ponto(Long id, String nome, String endereco, String tipoResiduo) {
        PontoDeColeta p = new PontoDeColeta();
        p.setId(id);
        p.setNome(nome);
        p.setEndereco(endereco);
        p.setTipoResiduo(tipoResiduo);
        return p;
    }

    @Test
    void getPontosDeveRetornar200ComTipoResiduo() throws Exception {
        when(service.listarTodos()).thenReturn(List.of(ponto(1L, "Ecoponto Centro", "Rua A, 10", "Vidro")));

        mockMvc.perform(get("/pontos"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].nome").value("Ecoponto Centro"))
                .andExpect(jsonPath("$[0].tipoResiduo").value("Vidro"));
    }

    @Test
    void postPontoDeveRetornar201() throws Exception {
        when(service.salvar(any(PontoDeColeta.class)))
                .thenReturn(ponto(1L, "Ecoponto Centro", "Rua A, 10", "Vidro"));

        mockMvc.perform(post("/pontos")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"nome\":\"Ecoponto Centro\",\"endereco\":\"Rua A, 10\",\"tipoResiduo\":\"Vidro\"}"))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.tipoResiduo").value("Vidro"));
    }

    @Test
    void getPontoInexistenteDeveRetornar404() throws Exception {
        when(service.buscarPorId(50L)).thenReturn(java.util.Optional.empty());

        mockMvc.perform(get("/pontos/50"))
                .andExpect(status().isNotFound());
    }
}
