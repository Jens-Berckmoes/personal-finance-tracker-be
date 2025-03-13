package be.jensberckmoes.personal_finance_tracker.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpMethod;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configurers.AbstractHttpConfigurer;
import org.springframework.security.web.SecurityFilterChain;

@Configuration
public class SecurityConfig {

    @Bean
    public SecurityFilterChain securityFilterChain(final HttpSecurity http) throws Exception {
        http
                .csrf(csrf -> csrf // Lambda-style configuration for CSRF
                        .ignoringRequestMatchers("/users/me") // Optionally ignore CSRF for specific endpoints
                )
                .authorizeHttpRequests(auth -> auth
                        .requestMatchers(HttpMethod.DELETE, "/users/**").hasRole("ADMIN") // Only Admins can delete users
                        .requestMatchers(HttpMethod.PUT, "/users/{id}/role").hasRole("ADMIN") // Role management for Admins
                        .requestMatchers("/users/me").authenticated() // Logged-in users can access their profile
                        .requestMatchers(HttpMethod.GET, "/users").hasRole("ADMIN") // Only Admins can view all users
                        .anyRequest().permitAll() // All other requests are open to everyone
                )
                .httpBasic(httpBasic -> httpBasic.realmName("Realm")); // HTTP Basic Auth config

        return http.build();
    }

}