package com.btvn.CaoMinhHuy.controllers;

import com.btvn.CaoMinhHuy.entities.Book;
import com.btvn.CaoMinhHuy.models.Cart;
import com.btvn.CaoMinhHuy.services.BookService;
import com.btvn.CaoMinhHuy.services.CartStorageService;
import com.btvn.CaoMinhHuy.services.OrderService;
import jakarta.servlet.http.HttpSession;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;

@Controller
@RequiredArgsConstructor
@RequestMapping("/cart")
public class CartController {

    private final BookService bookService;
    private final OrderService orderService;
    private final CartStorageService cartStorageService;

    private Cart getCart(HttpSession session) {
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        String username = (auth != null && auth.isAuthenticated() && !"anonymousUser".equals(auth.getName()))
                ? auth.getName() : null;

        if (username != null) {
            return cartStorageService.loadForUser(username);
        }

        Cart cart = (Cart) session.getAttribute("CART");
        if (cart == null) {
            cart = new Cart();
            session.setAttribute("CART", cart);
        }
        return cart;
    }

    @GetMapping
    public String viewCart(HttpSession session, Model model) {
        Cart cart = getCart(session);
        var statuses = new java.util.HashMap<Long, Boolean>();
        final boolean[] hasDiscontinued = {false};
        cart.getItems().values().forEach(it -> {
            try {
                var b = bookService.findById(it.getBookId());
                boolean disc = b.isDiscontinued();
                statuses.put(it.getBookId(), disc);
                if (disc) hasDiscontinued[0] = true;
            } catch (Exception e) {
                statuses.put(it.getBookId(), true);
                hasDiscontinued[0] = true;
            }
        });

        model.addAttribute("pageTitle", "Giỏ hàng");
        model.addAttribute("cart", cart);
        model.addAttribute("discontinuedMap", statuses);
        model.addAttribute("hasDiscontinued", hasDiscontinued[0]);
        model.addAttribute("content", "cart/cart :: content");
        return "layout/layout";
    }

    @PostMapping("/add/{id}")
    public String add(@PathVariable Long id, HttpSession session) {
        Book b = bookService.findById(id);
        Cart cart = getCart(session);
        cart.add(b.getId(), b.getTitle(), b.getPrice());

        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        if (auth != null && auth.isAuthenticated() && !"anonymousUser".equals(auth.getName())) {
            cartStorageService.addItem(auth.getName(), b);
        } else {
            session.setAttribute("CART", cart);
        }
        return "redirect:/cart";
    }

    @PostMapping("/increase/{id}")
    public String increase(@PathVariable Long id, HttpSession session) {
        Book b = bookService.findById(id);
        Cart cart = getCart(session);
        cart.add(b.getId(), b.getTitle(), b.getPrice());

        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        if (auth != null && auth.isAuthenticated() && !"anonymousUser".equals(auth.getName())) {
            cartStorageService.addItem(auth.getName(), b);
        } else {
            session.setAttribute("CART", cart);
        }
        return "redirect:/cart";
    }

    @PostMapping("/decrease/{id}")
    public String decrease(@PathVariable Long id, HttpSession session) {
        Cart cart = getCart(session);
        cart.decrease(id);

        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        if (auth != null && auth.isAuthenticated() && !"anonymousUser".equals(auth.getName())) {
            cartStorageService.decreaseItem(auth.getName(), id);
        } else {
            session.setAttribute("CART", cart);
        }
        return "redirect:/cart";
    }

    @PostMapping("/checkout")
    public String checkout(HttpSession session,
                           @RequestParam String shippingName,
                           @RequestParam String shippingPhone,
                            @RequestParam String shippingAddress,
                           @RequestParam(defaultValue = "COD") String paymentMethod,
                           @RequestParam(required = false) String couponCode) {
        Cart cart = getCart(session);
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        String username = auth != null ? auth.getName() : null;

        boolean hasDiscontinued = cart.getItems().values().stream().anyMatch(it -> {
            try {
                return bookService.findById(it.getBookId()).isDiscontinued();
            } catch (Exception e) {
                return true;
            }
        });
        if (hasDiscontinued) {
            return "redirect:/cart?error=Co%20san%20pham%20da%20ngung%20ban%2C%20vui%20long%20xoa%20khoi%20gio";
        }

        try {
            // Phí ship cố định 20.000, user không thay đổi được; discount từ frontend luôn 0
            var order = orderService.createFromCart(cart, username, shippingName, shippingPhone, shippingAddress, paymentMethod, 20000.0, 0.0, couponCode);
            cart.clear();
            if (username != null && !"anonymousUser".equals(username)) {
                cartStorageService.clear(username);
            } else {
                session.setAttribute("CART", cart);
            }
            return "redirect:/cart/checkout/success?orderId=" + order.getId();
        } catch (Exception ex) {
            String encoded = java.net.URLEncoder.encode(ex.getMessage(), java.nio.charset.StandardCharsets.UTF_8);
            return "redirect:/cart?error=" + encoded;
        }
    }

    @GetMapping("/checkout/success")
    public String checkoutSuccess(@RequestParam Long orderId, Model model) {
        model.addAttribute("pageTitle", "Thanh toán thành công");
        model.addAttribute("orderId", orderId);
        model.addAttribute("content", "cart/checkout_success :: content");
        return "layout/layout";
    }
}
