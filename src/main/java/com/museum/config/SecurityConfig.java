package com.museum.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpMethod;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.AuthenticationSuccessHandler;

@Configuration
public class SecurityConfig {
    private final AuthenticationSuccessHandler authenticationSuccessHandler;

    public SecurityConfig(CustomAuthenticationSuccessHandler authenticationSuccessHandler) {
        this.authenticationSuccessHandler = authenticationSuccessHandler;
    }

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
                        "/author.html",
                        "/events.html",
                        "/event.html",
                        "/exhibitions.html",
                        "/exhibition.html",
                        "/tickets.html",
                        "/visit.html",
                        "/about.html",
                        "/yandex_a8f128819e28930d.html",
                        "/favicon.ico",
                        "/favicon.png",
                        "/static/**",
                        "/css/**",
                        "/js/**",
                        "/images/**"
                ).permitAll()
                // Swagger доступен только для авторизованных пользователей
                .requestMatchers("/swagger-ui.html", "/swagger-ui/**", "/v3/api-docs/**").authenticated()
                // Страницы логина и регистрации
                .requestMatchers("/login", "/register").permitAll()

                // Публичные GET-запросы к API
                .requestMatchers(HttpMethod.GET, "/api/**").permitAll()

                // Покупка билетов доступна без аутентификации
                .requestMatchers(HttpMethod.POST, "/api/events/*/tickets/purchase").permitAll()

                // Доступ в админ-панель
                .requestMatchers("/admin/**").hasAnyRole("ADMIN", "MANAGER")
                
                // Статистика доступна только для ADMIN
                .requestMatchers("/admin/stats/**").hasRole("ADMIN")
                
                // Удаление через веб-интерфейс — только для ADMIN
                .requestMatchers(HttpMethod.POST, "/admin/**/delete").hasRole("ADMIN")

                // Изменения через API — только для ADMIN/MANAGER
                .requestMatchers(HttpMethod.POST, "/api/**").hasAnyRole("ADMIN", "MANAGER")
                .requestMatchers(HttpMethod.PUT, "/api/**").hasAnyRole("ADMIN", "MANAGER")
                // Удаление через API — только для ADMIN
                .requestMatchers(HttpMethod.DELETE, "/api/**").hasRole("ADMIN")

                // Остальное — требуется аутентификация
                .anyRequest().authenticated()
            )
            // Форма логина (сессии)
            .formLogin(form -> form
                .loginPage("/login").permitAll()
                // Используем кастомный обработчик для перенаправления:
                // администраторы и менеджеры идут в админку, остальные - на Swagger
                .successHandler(authenticationSuccessHandler)
            )
            .logout(logout -> logout
                .logoutRequestMatcher(
                    org.springframework.security.web.util.matcher.AntPathRequestMatcher.antMatcher(HttpMethod.GET, "/logout")
                )
                .logoutSuccessUrl("/")
                .invalidateHttpSession(true)
                .clearAuthentication(true)
                .deleteCookies("JSESSIONID")
                .permitAll()
            );

        return http.build();
    }

    @Bean
    public PasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder();
    }
}
