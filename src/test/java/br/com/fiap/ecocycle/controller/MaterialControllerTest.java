package br.com.fiap.ecocycle.controller;

import br.com.fiap.ecocycle.model.Material;
import br.com.fiap.ecocycle.service.MaterialService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;

import java.util.Optional;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

/**
 * Testes da camada HTTP do MaterialController com MockMvc standalone.
 */
@ExtendWith(MockitoExtension.class)
class MaterialControllerTest {

    @Mock
    private MaterialService service;

    @InjectMocks
    private MaterialController controller;

    private MockMvc mockMvc;

    @BeforeEach
    void setUp() {
        mockMvc = MockMvcBuilders.standaloneSetup(controller).build();
    }

    private static Material material(Long id, String nome, String tipo, Double peso) {
        Material m = new Material();
        m.setId(id);
        m.setNome(nome);
        m.setTipo(tipo);
        m.setPeso(peso);
        return m;
    }

    @Test
    void getMaterialExistenteDeveRetornar200() throws Exception {
        when(service.buscarPorId(1L)).thenReturn(Optional.of(material(1L, "Garrafa PET", "Plastico", 0.5)));

        mockMvc.perform(get("/materiais/1"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.nome").value("Garrafa PET"))
                .andExpect(jsonPath("$.tipo").value("Plastico"));
    }

    @Test
    void postMaterialDeveRetornar201() throws Exception {
        when(service.salvar(any(Material.class))).thenReturn(material(2L, "Lata", "Metal", 0.1));

        mockMvc.perform(post("/materiais")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"nome\":\"Lata\",\"tipo\":\"Metal\",\"peso\":0.1}"))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.nome").value("Lata"));
    }

    @Test
    void putMaterialExistenteDeveRetornar200() throws Exception {
        when(service.atualizar(eq(1L), any(Material.class)))
                .thenReturn(material(1L, "Lata de aluminio", "Aluminio", 0.25));

        mockMvc.perform(put("/materiais/1")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"nome\":\"Lata de aluminio\",\"tipo\":\"Aluminio\",\"peso\":0.25}"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.tipo").value("Aluminio"));
    }
}
