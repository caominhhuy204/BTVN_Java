package com.btvn.Mybook.controllers;

import com.btvn.Mybook.entities.Book;
import com.btvn.Mybook.models.Cart;
import com.btvn.Mybook.services.BookService;
import jakarta.servlet.http.HttpSession;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;

@Controller
@RequiredArgsConstructor
@RequestMapping("/cart")
public class CartController {

    private final BookService bookService;

    private Cart getCart(HttpSession session) {
        Cart cart = (Cart) session.getAttribute("CART");
        if (cart == null) {
            cart = new Cart();
            session.setAttribute("CART", cart);
        }
        return cart;
    }

    @GetMapping
    public String viewCart(HttpSession session, Model model) {
        model.addAttribute("pageTitle", "Giỏ hàng");
        model.addAttribute("cart", getCart(session));
        model.addAttribute("content", "cart/cart :: content");
        return "layout/layout";
    }

    @PostMapping("/add/{id}")
    public String add(@PathVariable Long id, HttpSession session) {
        Book b = bookService.findById(id);
        Cart cart = getCart(session);
        cart.add(b.getId(), b.getTitle(), b.getPrice());
        return "redirect:/cart";
    }

    @PostMapping("/checkout")
    public String checkout(HttpSession session, Model model) {
        Cart cart = getCart(session);
        cart.clear();
        model.addAttribute("pageTitle", "Checkout");
        model.addAttribute("content", "cart/checkout :: content");
        return "layout/layout";
    }
}