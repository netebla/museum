package com.museum.config;

import com.museum.model.User;
import com.museum.model.UserRole;
import com.museum.repository.UserRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.CommandLineRunner;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.crypto.password.PasswordEncoder;

@Configuration
public class DevAdminInitializer {
    private static final Logger log = LoggerFactory.getLogger(DevAdminInitializer.class);

    @Bean
    CommandLineRunner initDefaultAdmin(UserRepository users, PasswordEncoder encoder) {
        return args -> {
            if (users.findByUsername("admin").isEmpty()) {
                User u = new User();
                u.setUsername("admin");
                u.setPasswordHash(encoder.encode("admin"));
                u.setRole(UserRole.ADMIN);
                u.setEmail("admin@example.com");
                users.save(u);
                log.warn("Created default admin user: username=admin, password=admin (change in production!)");
            }
            if (users.findByUsername("manager").isEmpty()) {
                User u = new User();
                u.setUsername("manager");
                u.setPasswordHash(encoder.encode("manager"));
                u.setRole(UserRole.MANAGER);
                u.setEmail("manager@example.com");
                users.save(u);
                log.warn("Created default manager user: username=manager, password=manager (change in production!)");
            }
        };
    }
}

