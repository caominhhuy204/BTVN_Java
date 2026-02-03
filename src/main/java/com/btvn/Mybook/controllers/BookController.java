package com.btvn.Mybook.controllers;

import com.btvn.Mybook.entities.Book;
import com.btvn.Mybook.repositories.CategoryRepository;
import com.btvn.Mybook.services.BookService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.*;

@Controller
@RequiredArgsConstructor
@RequestMapping("/books")
public class BookController {

    private final BookService bookService;
    private final CategoryRepository categoryRepository;

    @GetMapping
    public String list(@RequestParam(required = false) String q, Model model) {
        model.addAttribute("pageTitle", "Danh sách sách");
        model.addAttribute("books", bookService.search(q));
        model.addAttribute("q", q);
        model.addAttribute("content", "books/list :: content");
        return "layout/layout";
    }

    @GetMapping("/new")
    public String createForm(Model model) {
        model.addAttribute("pageTitle", "Thêm sách");
        model.addAttribute("book", new Book());
        model.addAttribute("categories", categoryRepository.findAll());
        model.addAttribute("content", "books/form :: content");
        return "layout/layout";
    }

    @PostMapping
    public String create(@Valid @ModelAttribute("book") Book book, BindingResult br, Model model) {
        if (br.hasErrors()) {
            model.addAttribute("pageTitle", "Thêm sách");
            model.addAttribute("categories", categoryRepository.findAll());
            model.addAttribute("content", "books/form :: content");
            return "layout/layout";
        }
        bookService.save(book);
        return "redirect:/books";
    }

    @GetMapping("/{id}/edit")
    public String editForm(@PathVariable Long id, Model model) {
        model.addAttribute("pageTitle", "Cập nhật sách");
        model.addAttribute("book", bookService.findById(id));
        model.addAttribute("categories", categoryRepository.findAll());
        model.addAttribute("content", "books/form :: content");
        return "layout/layout";
    }

    @PostMapping("/{id}")
    public String update(@PathVariable Long id, @Valid @ModelAttribute("book") Book book, BindingResult br, Model model) {
        if (br.hasErrors()) {
            model.addAttribute("pageTitle", "Cập nhật sách");
            model.addAttribute("categories", categoryRepository.findAll());
            model.addAttribute("content", "books/form :: content");
            return "layout/layout";
        }
        book.setId(id);
        bookService.save(book);
        return "redirect:/books";
    }

    @PostMapping("/{id}/delete")
    public String delete(@PathVariable Long id) {
        bookService.deleteById(id);
        return "redirect:/books";
    }

    @GetMapping("/{id}/detail")
    public String detail(@PathVariable Long id, Model model) {
        model.addAttribute("pageTitle", "Chi tiết sách");
        model.addAttribute("book", bookService.findById(id));
        model.addAttribute("content", "books/detail :: content");
        return "layout/layout";
    }

    @GetMapping("/api-view")
    public String apiView(Model model) {
        model.addAttribute("pageTitle", "Danh sách sách (API)");
        model.addAttribute("content", "books/api_view :: content");
        return "layout/layout";
    }
}