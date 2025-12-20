package com.museum.config;

import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.web.authentication.SimpleUrlAuthenticationSuccessHandler;
import org.springframework.security.web.savedrequest.HttpSessionRequestCache;
import org.springframework.security.web.savedrequest.RequestCache;
import org.springframework.security.web.savedrequest.SavedRequest;
import org.springframework.stereotype.Component;

import java.io.IOException;
import java.util.Collection;

@Component
public class CustomAuthenticationSuccessHandler extends SimpleUrlAuthenticationSuccessHandler {
    private RequestCache requestCache = new HttpSessionRequestCache();

    @Override
    public void onAuthenticationSuccess(HttpServletRequest request, HttpServletResponse response,
                                        Authentication authentication) throws IOException, ServletException {
        Collection<? extends GrantedAuthority> authorities = authentication.getAuthorities();
        
        // Проверяем, является ли пользователь администратором или менеджером
        boolean isAdminOrManager = authorities.stream()
                .anyMatch(auth -> auth.getAuthority().equals("ROLE_ADMIN") || 
                                auth.getAuthority().equals("ROLE_MANAGER"));
        
        if (isAdminOrManager) {
            // Для администраторов и менеджеров всегда перенаправляем в админку
            SavedRequest savedRequest = requestCache.getRequest(request, response);
            if (savedRequest != null) {
                String targetUrl = savedRequest.getRedirectUrl();
                // Если сохраненный запрос ведет на страницу админки, используем его
                if (targetUrl != null && targetUrl.contains("/admin")) {
                    requestCache.removeRequest(request, response);
                    getRedirectStrategy().sendRedirect(request, response, targetUrl);
                    return;
                }
                // Если savedRequest ведет не в админку (например, на Swagger), удаляем его
                requestCache.removeRequest(request, response);
            }
            // Всегда перенаправляем в админку, игнорируя savedRequest
            setDefaultTargetUrl("/admin");
            setAlwaysUseDefaultTargetUrl(true);
        } else {
            // Обычных пользователей перенаправляем в личный кабинет
            SavedRequest savedRequest = requestCache.getRequest(request, response);
            if (savedRequest != null) {
                String targetUrl = savedRequest.getRedirectUrl();
                // Если сохраненный запрос ведет на Swagger, игнорируем его
                if (targetUrl != null && (targetUrl.contains("/swagger-ui") || targetUrl.contains("/v3/api-docs"))) {
                    requestCache.removeRequest(request, response);
                } else if (targetUrl != null && !targetUrl.contains("/admin")) {
                    // Если savedRequest ведет на обычную страницу (не админку и не Swagger), используем его
                    requestCache.removeRequest(request, response);
                    getRedirectStrategy().sendRedirect(request, response, targetUrl);
                    return;
                } else {
                    // Если savedRequest ведет в админку, удаляем его
                    requestCache.removeRequest(request, response);
                }
            }
            // Перенаправляем в личный кабинет
            setDefaultTargetUrl("/profile");
            setAlwaysUseDefaultTargetUrl(true);
        }
        
        super.onAuthenticationSuccess(request, response, authentication);
    }
}

