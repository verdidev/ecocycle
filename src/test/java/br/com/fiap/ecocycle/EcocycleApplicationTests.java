package br.com.fiap.ecocycle;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.condition.EnabledIfEnvironmentVariable;
import org.springframework.boot.test.context.SpringBootTest;

/**
 * Sobe o contexto completo da aplicacao contra um Oracle REAL
 * (Flyway executa a migration e o Hibernate valida o schema).
 *
 * So executa quando a variavel DB_URL estiver definida. No GitHub Actions o
 * job de testes sobe um Oracle Free como "service container" e define DB_URL.
 * Localmente, sem banco, o teste e ignorado e o "mvn test" continua passando.
 */
@SpringBootTest
@EnabledIfEnvironmentVariable(named = "DB_URL", matches = ".+")
class EcocycleApplicationTests {

	@Test
	void contextLoads() {
	}

}
