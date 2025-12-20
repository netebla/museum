package com.museum.controller;

import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

@ControllerAdvice
public class WebExceptionHandler {
    
    @ExceptionHandler(IllegalArgumentException.class)
    public String handleNotFound(IllegalArgumentException ex, RedirectAttributes redirectAttributes) {
        redirectAttributes.addFlashAttribute("error", "Запись не найдена: " + ex.getMessage());
        return "redirect:/admin";
    }
    
    @ExceptionHandler(IllegalStateException.class)
    public String handleConflict(IllegalStateException ex, RedirectAttributes redirectAttributes) {
        redirectAttributes.addFlashAttribute("error", "Ошибка: " + ex.getMessage());
        return "redirect:/admin";
    }
    
    @ExceptionHandler(Exception.class)
    public String handleGeneric(Exception ex, RedirectAttributes redirectAttributes) {
        redirectAttributes.addFlashAttribute("error", "Произошла ошибка: " + ex.getMessage());
        return "redirect:/admin";
    }
}

