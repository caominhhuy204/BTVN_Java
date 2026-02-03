package com.btvn.Mybook.controllers;

import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;

@Controller
public class ErrorPageController {

    @GetMapping("/error/403")
    public String forbidden(Model model) {
        model.addAttribute("pageTitle", "403 Forbidden");
        model.addAttribute("content", "error/403 :: content");
        return "layout/layout";
    }

    @GetMapping("/error/404")
    public String notFound(Model model) {
        model.addAttribute("pageTitle", "404 Not Found");
        model.addAttribute("content", "error/404 :: content");
        return "layout/layout";
    }
}