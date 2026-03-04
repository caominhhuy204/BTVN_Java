package com.btvn.CaoMinhHuy.controllers;

import com.btvn.CaoMinhHuy.entities.Category;
import com.btvn.CaoMinhHuy.services.CategoryService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;

@Controller
@RequiredArgsConstructor
@RequestMapping("/admin/categories")
public class AdminCategoryController {

    private final CategoryService categoryService;

    @GetMapping
    public String list(@RequestParam(required = false) String message,
                       @RequestParam(required = false) String error,
                       @RequestParam(defaultValue = "0") int page,
                       @RequestParam(defaultValue = "10") int size,
                       @RequestParam(defaultValue = "name") String sort,
                       @RequestParam(defaultValue = "asc") String dir,
                       Model model) {
        Sort.Direction direction = "asc".equalsIgnoreCase(dir) ? Sort.Direction.ASC : Sort.Direction.DESC;
        Pageable pageable = PageRequest.of(page, size, Sort.by(direction, sort));
        Page<Category> categoryPage = categoryService.findAll(pageable);
        Map<Long, Long> counts = categoryService.bookCountByCategory();

        model.addAttribute("pageTitle", "Danh mục");
        model.addAttribute("categories", categoryPage.getContent());
        model.addAttribute("categoryPage", categoryPage);
        model.addAttribute("size", size);
        model.addAttribute("sort", sort);
        model.addAttribute("dir", direction.isAscending() ? "asc" : "desc");
        model.addAttribute("categoryCounts", counts);
        model.addAttribute("message", message);
        model.addAttribute("error", error);
        model.addAttribute("content", "admin/categories/list :: content");
        return "layout/layout";
    }

    @GetMapping("/new")
    public String createForm(Model model) {
        model.addAttribute("pageTitle", "Thêm danh mục");
        model.addAttribute("category", new Category());
        model.addAttribute("content", "admin/categories/form :: content");
        return "layout/layout";
    }

    @PostMapping
    public String create(@Valid @ModelAttribute("category") Category category, BindingResult br, Model model, RedirectAttributes ra) {
        if (br.hasErrors()) {
            model.addAttribute("pageTitle", "Thêm danh mục");
            model.addAttribute("content", "admin/categories/form :: content");
            return "layout/layout";
        }
        try {
            categoryService.save(category);
        } catch (IllegalArgumentException | IllegalStateException ex) {
            model.addAttribute("pageTitle", "Thêm danh mục");
            model.addAttribute("error", ex.getMessage());
            model.addAttribute("content", "admin/categories/form :: content");
            return "layout/layout";
        }
        ra.addFlashAttribute("message", "Đã tạo danh mục");
        return "redirect:/admin/categories";
    }

    @GetMapping("/{id}/edit")
    public String editForm(@PathVariable Long id, Model model) {
        model.addAttribute("pageTitle", "Cập nhật danh mục");
        model.addAttribute("category", categoryService.findById(id));
        model.addAttribute("content", "admin/categories/form :: content");
        return "layout/layout";
    }

    @PostMapping("/{id}")
    public String update(@PathVariable Long id,
                         @Valid @ModelAttribute("category") Category category,
                         BindingResult br,
                         Model model,
                         RedirectAttributes ra) {
        if (br.hasErrors()) {
            model.addAttribute("pageTitle", "Cập nhật danh mục");
            model.addAttribute("content", "admin/categories/form :: content");
            return "layout/layout";
        }
        category.setId(id);
        try {
            categoryService.save(category);
        } catch (IllegalArgumentException | IllegalStateException ex) {
            model.addAttribute("pageTitle", "Cập nhật danh mục");
            model.addAttribute("error", ex.getMessage());
            model.addAttribute("content", "admin/categories/form :: content");
            return "layout/layout";
        }
        ra.addFlashAttribute("message", "Đã cập nhật");
        return "redirect:/admin/categories";
    }

    @GetMapping("/{id}/confirm-delete")
    public String confirmDelete(@PathVariable Long id,
                                @RequestParam(required = false) String error,
                                Model model) {
        Category category = categoryService.findById(id);
        long relatedBooks = categoryService.countBooks(id);
        List<Category> alternatives = categoryService.findAll().stream()
                .filter(c -> !c.getId().equals(id))
                .collect(Collectors.toList());

        model.addAttribute("pageTitle", "Xóa danh mục");
        model.addAttribute("category", category);
        model.addAttribute("relatedBooks", relatedBooks);
        model.addAttribute("alternatives", alternatives);
        model.addAttribute("error", error);
        model.addAttribute("content", "admin/categories/confirm_delete :: content");
        return "layout/layout";
    }

    @PostMapping("/{id}/delete")
    public String delete(@PathVariable Long id,
                         @RequestParam(required = false) Long targetCategoryId,
                         RedirectAttributes ra) {
        try {
            categoryService.delete(id, targetCategoryId);
            ra.addFlashAttribute("message", "Đã xóa");
            return "redirect:/admin/categories";
        } catch (IllegalStateException | IllegalArgumentException ex) {
            ra.addFlashAttribute("error", ex.getMessage());
            return "redirect:/admin/categories/{id}/confirm-delete";
        }
    }

    @PostMapping("/cleanup-empty")
    public String cleanupEmpty(RedirectAttributes ra) {
        int removed = categoryService.deleteEmptyCategories();
        ra.addFlashAttribute("message", "Đã xóa " + removed + " danh mục trống");
        return "redirect:/admin/categories";
    }
}
