package br.com.fiap.ecocycle;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.condition.EnabledIfEnvironmentVariable;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.test.context.SpringBootTest;

import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.nio.charset.StandardCharsets;
import java.util.Base64;
import java.util.UUID;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * Testes de integracao ponta a ponta: aplicacao real (porta aleatoria) + Oracle real.
 * Valida health check, seguranca (Basic Auth) e o CRUD persistindo no banco
 * criado pelo Flyway.
 *
 * So executa quando DB_URL estiver definida (ver EcocycleApplicationTests).
 */
@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@EnabledIfEnvironmentVariable(named = "DB_URL", matches = ".+")
class EcocycleIntegrationTests {

    private static final Pattern ID_PATTERN = Pattern.compile("\"id\"\\s*:\\s*(\\d+)");

    private final HttpClient http = HttpClient.newBuilder()
            .version(HttpClient.Version.HTTP_1_1)
            .build();

    @Value("${local.server.port}")
    private int port;

    @Value("${spring.security.user.name}")
    private String username;

    @Value("${spring.security.user.password}")
    private String password;

    private HttpResponse<String> send(String method, String path, String json, boolean autenticado) throws Exception {
        HttpRequest.Builder builder = HttpRequest.newBuilder(URI.create("http://localhost:" + port + path));
        if (autenticado) {
            String token = Base64.getEncoder()
                    .encodeToString((username + ":" + password).getBytes(StandardCharsets.UTF_8));
            builder.header("Authorization", "Basic " + token);
        }
        if (json != null) {
            builder.header("Content-Type", "application/json");
            builder.method(method, HttpRequest.BodyPublishers.ofString(json));
        } else {
            builder.method(method, HttpRequest.BodyPublishers.noBody());
        }
        return http.send(builder.build(), HttpResponse.BodyHandlers.ofString());
    }

    private static long extrairId(String body) {
        Matcher m = ID_PATTERN.matcher(body);
        assertThat(m.find()).as("resposta deveria conter um id: %s", body).isTrue();
        return Long.parseLong(m.group(1));
    }

    @Test
    void healthDeveEstarUpSemAutenticacao() throws Exception {
        HttpResponse<String> resp = send("GET", "/actuator/health", null, false);

        assertThat(resp.statusCode()).isEqualTo(200);
        assertThat(resp.body()).contains("UP");
    }

    @Test
    void endpointsDeNegocioExigemAutenticacao() throws Exception {
        assertThat(send("GET", "/usuarios", null, false).statusCode()).isEqualTo(401);
        assertThat(send("GET", "/materiais", null, false).statusCode()).isEqualTo(401);
        assertThat(send("GET", "/pontos", null, false).statusCode()).isEqualTo(401);
    }

    @Test
    void crudCompletoDeUsuarioNoOracle() throws Exception {
        String email = "it-" + UUID.randomUUID() + "@ecocycle.test";

        HttpResponse<String> criado = send("POST", "/usuarios",
                "{\"nome\":\"Usuario IT\",\"email\":\"" + email + "\"}", true);
        assertThat(criado.statusCode()).isEqualTo(201);
        long id = extrairId(criado.body());

        HttpResponse<String> lido = send("GET", "/usuarios/" + id, null, true);
        assertThat(lido.statusCode()).isEqualTo(200);
        assertThat(lido.body()).contains(email);

        HttpResponse<String> atualizado = send("PUT", "/usuarios/" + id,
                "{\"nome\":\"Usuario IT Atualizado\",\"email\":\"" + email + "\"}", true);
        assertThat(atualizado.statusCode()).isEqualTo(200);
        assertThat(atualizado.body()).contains("Usuario IT Atualizado");

        assertThat(send("DELETE", "/usuarios/" + id, null, true).statusCode()).isEqualTo(204);
        assertThat(send("GET", "/usuarios/" + id, null, true).statusCode()).isEqualTo(404);
    }

    @Test
    void materialPersistePesoDecimal() throws Exception {
        HttpResponse<String> criado = send("POST", "/materiais",
                "{\"nome\":\"Garrafa PET\",\"tipo\":\"Plastico\",\"peso\":1.75}", true);
        assertThat(criado.statusCode()).isEqualTo(201);
        long id = extrairId(criado.body());

        HttpResponse<String> lido = send("GET", "/materiais/" + id, null, true);
        assertThat(lido.statusCode()).isEqualTo(200);
        assertThat(lido.body()).contains("1.75");

        send("DELETE", "/materiais/" + id, null, true);
    }

    @Test
    void pontoDeColetaPersisteTipoResiduo() throws Exception {
        HttpResponse<String> criado = send("POST", "/pontos",
                "{\"nome\":\"Ecoponto IT\",\"endereco\":\"Rua Teste, 1\",\"tipoResiduo\":\"Eletronicos\"}", true);
        assertThat(criado.statusCode()).isEqualTo(201);
        long id = extrairId(criado.body());

        // Le de novo do banco: antes da correcao o campo era @Transient e voltava null
        HttpResponse<String> lido = send("GET", "/pontos/" + id, null, true);
        assertThat(lido.statusCode()).isEqualTo(200);
        assertThat(lido.body()).contains("Eletronicos");

        send("DELETE", "/pontos/" + id, null, true);
    }
}
