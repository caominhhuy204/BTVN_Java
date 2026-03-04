package com.btvn.CaoMinhHuy.controllers;

import com.btvn.CaoMinhHuy.services.WishlistService;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

@Controller
@RequiredArgsConstructor
public class WishlistController {
    private final WishlistService wishlistService;

    @GetMapping("/wishlist")
    public String list(Authentication auth, Model model) {
        var items = wishlistService.list(auth.getName());
        model.addAttribute("pageTitle", "Yêu thích");
        model.addAttribute("items", items);
        model.addAttribute("content", "wishlist/list :: content");
        return "layout/layout";
    }

    @PostMapping("/wishlist/toggle")
    public String toggle(@RequestParam Long bookId, Authentication auth, RedirectAttributes ra) {
        try {
            var item = wishlistService.toggle(bookId, auth.getName());
            ra.addFlashAttribute("message", item == null ? "Đã bỏ yêu thích" : "Đã thêm vào yêu thích");
        } catch (Exception e) {
            ra.addFlashAttribute("error", e.getMessage());
        }
        return "redirect:/books/" + bookId + "/detail";
    }
}
