package com.museum.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpMethod;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;

@Configuration
public class SecurityConfig {
    @Bean
    public SecurityFilterChain filterChain(HttpSecurity http) throws Exception {
        http
            // Оставляем CSRF включённым по умолчанию, но выключаем для API,
            // чтобы Swagger/клиенты могли вызывать JSON-эндпоинты без токена
            .csrf(csrf -> csrf
                .ignoringRequestMatchers(
                    "/api/**",
                    "/swagger-ui/**",
                    "/v3/api-docs/**"
                )
            )
            .authorizeHttpRequests(auth -> auth
                // Публичные ресурсы
                .requestMatchers(
                        "/",
                        "/index.html",
                        "/static/**",
                        "/css/**",
                        "/js/**",
                        "/images/**",
                        "/swagger-ui.html",
                        "/swagger-ui/**",
                        "/v3/api-docs/**"
                ).permitAll()
                // Страница логина
                .requestMatchers("/login").permitAll()

                // Публичные GET-запросы к API
                .requestMatchers(HttpMethod.GET, "/api/**").permitAll()

                // Покупка билетов доступна без аутентификации
                .requestMatchers(HttpMethod.POST, "/api/events/*/tickets/purchase").permitAll()

                // Доступ в админ-панель
                .requestMatchers("/admin/**").hasAnyRole("ADMIN", "MANAGER")

                // Изменения через API — только для ADMIN/MANAGER
                .requestMatchers(HttpMethod.POST, "/api/**").hasAnyRole("ADMIN", "MANAGER")
                .requestMatchers(HttpMethod.PUT, "/api/**").hasAnyRole("ADMIN", "MANAGER")
                .requestMatchers(HttpMethod.DELETE, "/api/**").hasAnyRole("ADMIN", "MANAGER")

                // Остальное — требуется аутентификация
                .anyRequest().authenticated()
            )
            // Форма логина (сессии)
            .formLogin(form -> form
                .loginPage("/login").permitAll()
                .defaultSuccessUrl("/swagger-ui.html", true)
            )
            .logout(logout -> logout.logoutUrl("/logout").logoutSuccessUrl("/").permitAll());

        return http.build();
    }

    @Bean
    public PasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder();
    }
}
