package br.com.fiap.ecocycle.controller;

import br.com.fiap.ecocycle.model.Usuario;
import br.com.fiap.ecocycle.service.UsuarioService;
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
import java.util.Optional;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

/**
 * Testes da camada HTTP do UsuarioController com MockMvc standalone:
 * valida rotas, codigos de status e JSON sem subir o contexto Spring nem o banco.
 */
@ExtendWith(MockitoExtension.class)
class UsuarioControllerTest {

    @Mock
    private UsuarioService service;

    @InjectMocks
    private UsuarioController controller;

    private MockMvc mockMvc;

    @BeforeEach
    void setUp() {
        mockMvc = MockMvcBuilders.standaloneSetup(controller).build();
    }

    private static Usuario usuario(Long id, String nome, String email) {
        Usuario u = new Usuario();
        u.setId(id);
        u.setNome(nome);
        u.setEmail(email);
        return u;
    }

    @Test
    void getUsuariosDeveRetornar200ComALista() throws Exception {
        when(service.listarTodos()).thenReturn(List.of(usuario(1L, "Ana", "ana@email.com")));

        mockMvc.perform(get("/usuarios"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].nome").value("Ana"))
                .andExpect(jsonPath("$[0].email").value("ana@email.com"));
    }

    @Test
    void getUsuarioInexistenteDeveRetornar404() throws Exception {
        when(service.buscarPorId(99L)).thenReturn(Optional.empty());

        mockMvc.perform(get("/usuarios/99"))
                .andExpect(status().isNotFound());
    }

    @Test
    void postUsuarioDeveRetornar201() throws Exception {
        when(service.salvar(any(Usuario.class))).thenReturn(usuario(1L, "Ana", "ana@email.com"));

        mockMvc.perform(post("/usuarios")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"nome\":\"Ana\",\"email\":\"ana@email.com\"}"))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.nome").value("Ana"));
    }

    @Test
    void putUsuarioInexistenteDeveRetornar404() throws Exception {
        when(service.atualizar(eq(99L), any(Usuario.class)))
                .thenThrow(new RuntimeException("Usuário não encontrado"));

        mockMvc.perform(put("/usuarios/99")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"nome\":\"X\",\"email\":\"x@email.com\"}"))
                .andExpect(status().isNotFound());
    }

    @Test
    void deleteUsuarioDeveRetornar204() throws Exception {
        mockMvc.perform(delete("/usuarios/1"))
                .andExpect(status().isNoContent());

        verify(service).excluir(1L);
    }
}
