package be.jensberckmoes.personal_finance_tracker.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpMethod;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.web.SecurityFilterChain;

@Configuration
public class SecurityConfig {

    @Bean
    public SecurityFilterChain securityFilterChain(final HttpSecurity http) throws Exception {
        http
                .csrf(csrf -> csrf.ignoringRequestMatchers("/users/me"))
                .authorizeHttpRequests(auth -> auth
                        .requestMatchers(HttpMethod.PUT, "/users/{id}/role").hasRole("ADMIN")
                        .requestMatchers(HttpMethod.DELETE, "/users/**").hasRole("ADMIN")
                        .requestMatchers(HttpMethod.GET, "/users").hasRole("ADMIN")

                        .requestMatchers("/users/me").authenticated()

                        .requestMatchers(HttpMethod.POST, "/users").permitAll()

                        .anyRequest().permitAll()
                )
                .httpBasic(httpBasic -> httpBasic.realmName("Realm"));

        return http.build();
    }

}