package br.com.fiap.ecocycle.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.config.Customizer;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.web.SecurityFilterChain;

@Configuration
@EnableWebSecurity
public class SecurityConfig {

    @Bean
    public SecurityFilterChain securityFilterChain(HttpSecurity http) throws Exception {
        http
                .csrf(csrf -> csrf.disable()) // API REST stateless consumida via Basic Auth
                .authorizeHttpRequests(auth -> auth
                        // Health check publico: usado pelo Docker HEALTHCHECK, Compose e smoke test do pipeline.
                        // Nao expoe detalhes (management.endpoint.health.show-details=never).
                        .requestMatchers("/actuator/health", "/actuator/health/**").permitAll()
                        .anyRequest().authenticated() // Todo o resto continua exigindo login
                )
                .httpBasic(Customizer.withDefaults());

        return http.build();
    }
}
