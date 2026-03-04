package com.btvn.CaoMinhHuy.controllers;

import com.btvn.CaoMinhHuy.services.ReviewService;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

@Controller
@RequiredArgsConstructor
public class ReviewController {
    private final ReviewService reviewService;

    @PostMapping("/books/{id}/reviews")
    public String addReview(@PathVariable Long id,
                            @RequestParam(required = false) Integer rating,
                            @RequestParam String comment,
                            @RequestParam(required = false) Long parentId,
                            Authentication auth,
                            RedirectAttributes ra) {
        try {
            String username = auth != null ? auth.getName() : "Ẩn danh";
            if (parentId != null) {
                reviewService.addReply(id, parentId, username, rating, comment);
                ra.addFlashAttribute("message", "Đã gửi phản hồi");
            } else {
                reviewService.addReview(id, username, rating, comment);
                ra.addFlashAttribute("message", "Đã gửi đánh giá");
            }
        } catch (Exception e) {
            ra.addFlashAttribute("error", e.getMessage());
        }
        return "redirect:/books/" + id + "/detail";
    }
}
