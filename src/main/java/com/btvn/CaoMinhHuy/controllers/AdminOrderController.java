package com.btvn.CaoMinhHuy.controllers;

import com.btvn.CaoMinhHuy.services.OrderService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

@Controller
@RequiredArgsConstructor
@RequestMapping("/admin/orders")
public class AdminOrderController {

    private final OrderService orderService;

    @GetMapping
    public String list(Model model,
                       @RequestParam(required = false) String message,
                       @RequestParam(required = false) String error) {
        model.addAttribute("pageTitle", "Đơn hàng");
        model.addAttribute("orders", orderService.findAll());
        model.addAttribute("message", message);
        model.addAttribute("error", error);
        model.addAttribute("content", "admin/orders/list :: content");
        return "layout/layout";
    }

    @GetMapping("/{id}")
    public String detail(@PathVariable Long id, Model model) {
        try {
            var order = orderService.findById(id);
            model.addAttribute("pageTitle", "Chi tiết đơn #" + id);
            model.addAttribute("order", order);
            model.addAttribute("content", "admin/orders/detail :: content");
            return "layout/layout";
        } catch (IllegalArgumentException ex) {
            return "redirect:/admin/orders?error=" + java.net.URLEncoder.encode(ex.getMessage(), java.nio.charset.StandardCharsets.UTF_8);
        }
    }

    @GetMapping("/{id}/page")
    public String detailPage(@PathVariable Long id, Model model,
                             @RequestParam(required = false) String message,
                             @RequestParam(required = false) String error) {
        try {
            var order = orderService.findById(id);
            model.addAttribute("pageTitle", "Chi tiết đơn #" + id);
            model.addAttribute("order", order);
            model.addAttribute("message", message);
            model.addAttribute("error", error);
            model.addAttribute("content", "admin/orders/admin_detail :: content");
            return "layout/layout";
        } catch (IllegalArgumentException ex) {
            return "redirect:/admin/orders?error=" + java.net.URLEncoder.encode(ex.getMessage(), java.nio.charset.StandardCharsets.UTF_8);
        }
    }

    @PostMapping("/{id}/status")
    public String updateStatus(@PathVariable Long id,
                               @RequestParam String status,
                               @RequestParam(required = false) String adminNote,
                               RedirectAttributes ra) {
        try {
            orderService.updateStatus(id, status, adminNote);
            ra.addFlashAttribute("message", "Đã cập nhật trạng thái đơn #" + id + " thành " + status);
        } catch (Exception ex) {
            ra.addFlashAttribute("error", "Cập nhật trạng thái thất bại: " + ex.getMessage());
        }
        return "redirect:/admin/orders/" + id;
    }
}
