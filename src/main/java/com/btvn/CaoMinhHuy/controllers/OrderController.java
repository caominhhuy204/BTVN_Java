package com.btvn.CaoMinhHuy.controllers;

import com.btvn.CaoMinhHuy.services.OrderService;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;

@Controller
@RequiredArgsConstructor
@RequestMapping("/orders")
public class OrderController {

    private final OrderService orderService;

    @GetMapping
    public String list(Authentication auth, Model model) {
        String username = auth != null ? auth.getName() : null;
        model.addAttribute("pageTitle", "Lịch sử đơn hàng");
        model.addAttribute("orders", orderService.findForUser(username));
        model.addAttribute("content", "orders/list :: content");
        return "layout/layout";
    }

    @GetMapping("/{id}")
    public String detail(@PathVariable Long id, Authentication auth, Model model) {
        String username = auth != null ? auth.getName() : null;
        if (username == null) {
            return "redirect:/auth/login";
        }
        model.addAttribute("pageTitle", "Chi tiết đơn #" + id);
        model.addAttribute("order", orderService.findByIdForUser(id, username));
        model.addAttribute("content", "orders/detail :: content");
        return "layout/layout";
    }
}
