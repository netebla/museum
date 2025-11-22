package com.museum.controller;

import com.museum.dto.RegistrationRequest;
import com.museum.model.User;
import com.museum.model.UserRole;
import com.museum.repository.UserRepository;
import jakarta.validation.Valid;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PostMapping;

@Controller
public class RegistrationController {
    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;

    public RegistrationController(UserRepository userRepository, PasswordEncoder passwordEncoder) {
        this.userRepository = userRepository;
        this.passwordEncoder = passwordEncoder;
    }

    @GetMapping("/register")
    public String showForm(Model model) {
        model.addAttribute("form", new RegistrationRequest());
        return "register";
    }

    @PostMapping("/register")
    public String register(@Valid @ModelAttribute("form") RegistrationRequest form,
                           BindingResult bindingResult,
                           Model model) {
        if (!bindingResult.hasErrors()) {
            String email = form.getEmail().trim().toLowerCase();
            if (userRepository.findByEmail(email).isPresent()) {
                bindingResult.rejectValue("email", "email.exists", "Пользователь с такой почтой уже зарегистрирован");
            }
        }

        if (bindingResult.hasErrors()) {
            return "register";
        }

        String email = form.getEmail().trim().toLowerCase();
        User user = new User();
        user.setUsername(email); // логин по почте
        user.setEmail(email);
        user.setFullName(form.getFullName());
        user.setRole(UserRole.VISITOR);
        user.setPasswordHash(passwordEncoder.encode(form.getPassword()));
        userRepository.save(user);

        model.addAttribute("registeredEmail", email);
        return "register_success";
    }
}

