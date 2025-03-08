package be.jensberckmoes.personal_finance_tracker.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configurers.AbstractHttpConfigurer;
import org.springframework.security.web.SecurityFilterChain;

@Configuration
public class SecurityConfig {

    @Bean
    public SecurityFilterChain securityFilterChain(final HttpSecurity http) throws Exception {
        http
                .csrf(AbstractHttpConfigurer::disable) // Disable CSRF for now
                .authorizeHttpRequests(auth -> auth
                        .requestMatchers("/users").permitAll() // Allow public access to POST /users
                        .anyRequest().authenticated() // Require authentication for all other endpoints
                )
                .httpBasic(httpBasic -> httpBasic.realmName("Realm")); // Explicitly configure HTTP Basic authentication

        return http.build();
    }
}

