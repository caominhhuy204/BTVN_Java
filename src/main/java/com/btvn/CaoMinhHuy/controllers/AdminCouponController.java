package com.btvn.CaoMinhHuy.controllers;

import com.btvn.CaoMinhHuy.entities.Coupon;
import com.btvn.CaoMinhHuy.repositories.CouponRepository;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

@Controller
@RequiredArgsConstructor
@RequestMapping("/admin/coupons")
public class AdminCouponController {

    private final CouponRepository couponRepository;

    @GetMapping
    public String list(Model model) {
        return renderList(model, new Coupon());
    }

    @GetMapping("/{id}/edit")
    public String edit(@PathVariable Long id, Model model, RedirectAttributes ra) {
        return couponRepository.findById(id)
                .map(c -> renderList(model, c))
                .orElseGet(() -> {
                    ra.addFlashAttribute("error", "Không tìm thấy mã giảm giá");
                    return "redirect:/admin/coupons";
                });
    }

    private String renderList(Model model, Coupon formCoupon) {
        model.addAttribute("pageTitle", "Mã giảm giá");
        model.addAttribute("coupons", couponRepository.findAll());
        model.addAttribute("coupon", formCoupon);
        model.addAttribute("content", "admin/coupons/list :: content");
        return "layout/layout";
    }

    @PostMapping
    public String create(@Valid @ModelAttribute("coupon") Coupon coupon,
                         BindingResult br,
                         RedirectAttributes ra) {
        if (br.hasErrors()) {
            ra.addFlashAttribute("error", "Dữ liệu không hợp lệ");
            return "redirect:/admin/coupons";
        }
        String code = coupon.getCode() != null ? coupon.getCode().trim() : null;
        if (code == null || code.isBlank()) {
            ra.addFlashAttribute("error", "Mã không được để trống");
            return "redirect:/admin/coupons";
        }
        if (couponRepository.existsByCodeIgnoreCase(code)) {
            ra.addFlashAttribute("error", "Mã đã tồn tại");
            return "redirect:/admin/coupons";
        }
        if (invalidDateRange(coupon)) {
            ra.addFlashAttribute("error", "Ngày kết thúc phải sau ngày bắt đầu");
            return "redirect:/admin/coupons";
        }
        if (coupon.getQuantity() == null || coupon.getQuantity() < 0) {
            ra.addFlashAttribute("error", "Số lượng phải >= 0");
            return "redirect:/admin/coupons";
        }
        coupon.setCode(code.toUpperCase());
        couponRepository.save(coupon);
        ra.addFlashAttribute("message", "Đã tạo mã " + coupon.getCode());
        return "redirect:/admin/coupons";
    }

    @PostMapping("/{id}")
    public String update(@PathVariable Long id,
                         @Valid @ModelAttribute("coupon") Coupon coupon,
                         BindingResult br,
                         RedirectAttributes ra) {
        if (br.hasErrors()) {
            ra.addFlashAttribute("error", "Dữ liệu không hợp lệ");
            return "redirect:/admin/coupons/" + id + "/edit";
        }
        String code = coupon.getCode() != null ? coupon.getCode().trim() : null;
        if (code == null || code.isBlank()) {
            ra.addFlashAttribute("error", "Mã không được để trống");
            return "redirect:/admin/coupons/" + id + "/edit";
        }
        if (couponRepository.existsByCodeIgnoreCaseAndIdNot(code, id)) {
            ra.addFlashAttribute("error", "Mã đã tồn tại");
            return "redirect:/admin/coupons/" + id + "/edit";
        }
        if (invalidDateRange(coupon)) {
            ra.addFlashAttribute("error", "Ngày kết thúc phải sau ngày bắt đầu");
            return "redirect:/admin/coupons/" + id + "/edit";
        }
        if (coupon.getQuantity() == null || coupon.getQuantity() < 0) {
            ra.addFlashAttribute("error", "Số lượng phải >= 0");
            return "redirect:/admin/coupons/" + id + "/edit";
        }

        return couponRepository.findById(id)
                .map(existing -> {
                    existing.setCode(code.toUpperCase());
                    existing.setAmount(coupon.getAmount());
                    existing.setActive(coupon.isActive());
                    existing.setStartAt(coupon.getStartAt());
                    existing.setEndAt(coupon.getEndAt());
                    existing.setQuantity(coupon.getQuantity());
                    couponRepository.save(existing);
                    ra.addFlashAttribute("message", "Đã cập nhật mã " + existing.getCode());
                    return "redirect:/admin/coupons";
                })
                .orElseGet(() -> {
                    ra.addFlashAttribute("error", "Không tìm thấy mã giảm giá");
                    return "redirect:/admin/coupons";
                });
    }

    @PostMapping("/{id}/toggle")
    public String toggle(@PathVariable Long id,
                         RedirectAttributes ra) {
        couponRepository.findById(id).ifPresentOrElse(c -> {
            c.setActive(!c.isActive());
            couponRepository.save(c);
            ra.addFlashAttribute("message", "Đã " + (c.isActive() ? "bật" : "tắt") + " mã " + c.getCode());
        }, () -> ra.addFlashAttribute("error", "Không tìm thấy mã giảm giá"));
        return "redirect:/admin/coupons";
    }

    private boolean invalidDateRange(Coupon coupon) {
        return coupon.getStartAt() != null
                && coupon.getEndAt() != null
                && coupon.getEndAt().isBefore(coupon.getStartAt());
    }
}
