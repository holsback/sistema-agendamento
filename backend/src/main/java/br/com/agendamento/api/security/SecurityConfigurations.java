package br.com.agendamento.api.security;

import org.springframework.web.cors.CorsConfiguration;
import org.springframework.web.cors.CorsConfigurationSource;
import org.springframework.web.cors.UrlBasedCorsConfigurationSource;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpMethod;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.config.annotation.authentication.configuration.AuthenticationConfiguration;
import org.springframework.security.config.annotation.method.configuration.EnableMethodSecurity;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;
import org.springframework.security.config.Customizer;
import org.springframework.security.config.annotation.web.configurers.HeadersConfigurer.FrameOptionsConfig;
import org.springframework.security.web.header.writers.ReferrerPolicyHeaderWriter.ReferrerPolicy;

import java.util.List;

@Configuration
@EnableWebSecurity
@EnableMethodSecurity // Habilita o @PreAuthorize
public class SecurityConfigurations {

    @Autowired
    private SecurityFilter securityFilter;

    /**
     * Define a cadeia de filtros de segurança principal.
     */
    @Bean
    public SecurityFilterChain securityFilterChain(HttpSecurity http) throws Exception {
        return http
                // Desabilita CSRF (API é stateless, o JWT já protege)
                .csrf(csrf -> csrf.disable())

                // Aplica configuração personalizada de CORS
                .cors(cors -> cors.configurationSource(corsConfigurationSource()))

                // Define a política de sessão como STATELESS (não guarda sessão)
                .sessionManagement(session -> session.sessionCreationPolicy(SessionCreationPolicy.STATELESS))

                // Define as regras de autorização de URL
                .authorizeHttpRequests(authorize -> authorize
                        // URLs públicas:
                        .requestMatchers(HttpMethod.POST, "/auth/login").permitAll()
                        .requestMatchers(HttpMethod.POST, "/auth/registrar").permitAll()
                        .requestMatchers("/h2-console/**").permitAll()

                        // (Verifique se estas URLs devem ser públicas ou privadas)
                        .requestMatchers("/configuracao").permitAll()
                        .requestMatchers("/usuarios/{idProfissional}/disponibilidade").permitAll()

                        // Todas as outras URLs exigem login
                        .anyRequest().authenticated()
                )
                // Adiciona nosso filtro de Token JWT antes do filtro de login padrão
                .addFilterBefore(securityFilter, UsernamePasswordAuthenticationFilter.class)

                // --- CABEÇALHOS DE SEGURANÇA UNIFICADOS ---
                .headers(headers -> headers

                        // 1. HSTS: Força o navegador a usar HTTPS por 1 ano
                        .httpStrictTransportSecurity(hsts -> hsts
                                .includeSubDomains(true)
                                .maxAgeInSeconds(31536000)
                        )

                        // 2. Content-Type-Options: Impede "adivinhação" do tipo do arquivo
                        .contentTypeOptions(Customizer.withDefaults())

                        // 3. Referrer-Policy: Controla a informação de "origem"
                        .referrerPolicy(policy -> policy
                                .policy(ReferrerPolicy.STRICT_ORIGIN_WHEN_CROSS_ORIGIN)
                        )

                        // 4. Frame-Options: Proteção contra Clickjacking
                        .frameOptions(FrameOptionsConfig::disable)
                )
                .build();
    }

    /**
     * Expõe o AuthenticationManager
     */
    @Bean
    public AuthenticationManager authenticationManager(AuthenticationConfiguration configuration) throws Exception {
        return configuration.getAuthenticationManager();
    }

    /**
     * Define o Criptografador de Senhas (BCrypt)
     */
    @Bean
    public PasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder();
    }

    /**
     * Configuração do CORS (Quais frontends podem falar com a API)
     */
    @Bean
    public CorsConfigurationSource corsConfigurationSource() {
        CorsConfiguration configuration = new CorsConfiguration();

        configuration.setAllowedOrigins(List.of(
                "http://localhost:3000",
                "http://localhost:5173",
                "http://localhost:4200"
        ));

        // (Já inclui o 'PATCH')
        configuration.setAllowedMethods(List.of(
                "GET", "POST", "PUT", "DELETE", "OPTIONS", "PATCH"
        ));

        configuration.setAllowedHeaders(List.of("Authorization", "Content-Type"));

        UrlBasedCorsConfigurationSource source = new UrlBasedCorsConfigurationSource();
        source.registerCorsConfiguration("/**", configuration);

        return source;
    }
}