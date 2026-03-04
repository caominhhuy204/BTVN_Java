package com.btvn.CaoMinhHuy.controllers;

import com.btvn.CaoMinhHuy.entities.Book;
import com.btvn.CaoMinhHuy.repositories.CategoryRepository;
import com.btvn.CaoMinhHuy.services.BookService;
import com.btvn.CaoMinhHuy.services.FileStorageService;
import com.btvn.CaoMinhHuy.services.ReviewService;
import com.btvn.CaoMinhHuy.services.WishlistService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

@Controller
@RequiredArgsConstructor
@RequestMapping("/books")
public class BookController {

    private final BookService bookService;
    private final CategoryRepository categoryRepository;
    private final FileStorageService fileStorageService;
    private final ReviewService reviewService;
    private final WishlistService wishlistService;

    @GetMapping
    public String list(@RequestParam(required = false) String q,
                       @RequestParam(required = false) Long categoryId,
                       @RequestParam(required = false) Double minPrice,
                       @RequestParam(required = false) Double maxPrice,
                       @RequestParam(defaultValue = "0") int page,
                       @RequestParam(defaultValue = "4") int size,
                       @RequestParam(defaultValue = "id") String sort,
                       @RequestParam(defaultValue = "desc") String dir,
                       Model model) {
        Sort.Direction direction = "asc".equalsIgnoreCase(dir) ? Sort.Direction.ASC : Sort.Direction.DESC;
        Pageable pageable = PageRequest.of(page, size, Sort.by(direction, sort));
        Page<Book> booksPage = bookService.search(q, categoryId, minPrice, maxPrice, pageable);

        model.addAttribute("pageTitle", "Danh sách sách");
        model.addAttribute("booksPage", booksPage);
        model.addAttribute("books", booksPage.getContent());
        model.addAttribute("q", q);
        model.addAttribute("categoryId", categoryId);
        model.addAttribute("minPrice", minPrice);
        model.addAttribute("maxPrice", maxPrice);
        model.addAttribute("size", size);
        model.addAttribute("sort", sort);
        model.addAttribute("dir", direction.isAscending() ? "asc" : "desc");
        model.addAttribute("categories", categoryRepository.findAll());
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
    public String create(@Valid @ModelAttribute("book") Book book,
                         BindingResult br,
                         @RequestParam(value = "coverFile", required = false) MultipartFile coverFile,
                         Model model) {
        if (br.hasErrors()) {
            model.addAttribute("pageTitle", "Thêm sách");
            model.addAttribute("categories", categoryRepository.findAll());
            model.addAttribute("content", "books/form :: content");
            return "layout/layout";
        }
        if (coverFile != null && !coverFile.isEmpty()) {
            book.setCoverUrl(fileStorageService.saveCover(coverFile));
        } else if (book.getCoverUrl() != null && book.getCoverUrl().isBlank()) {
            book.setCoverUrl(null);
        } else {
            book.setCoverUrl(normalizeCoverUrl(book.getCoverUrl()));
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
    public String update(@PathVariable Long id,
                         @Valid @ModelAttribute("book") Book book,
                         BindingResult br,
                         @RequestParam(value = "coverFile", required = false) MultipartFile coverFile,
                         Model model) {
        if (br.hasErrors()) {
            model.addAttribute("pageTitle", "Cập nhật sách");
            model.addAttribute("categories", categoryRepository.findAll());
            model.addAttribute("content", "books/form :: content");
            return "layout/layout";
        }
        book.setId(id);
        if (coverFile != null && !coverFile.isEmpty()) {
            book.setCoverUrl(fileStorageService.saveCover(coverFile));
        } else if (book.getCoverUrl() != null && book.getCoverUrl().isBlank()) {
            book.setCoverUrl(null);
        } else {
            book.setCoverUrl(normalizeCoverUrl(book.getCoverUrl()));
        }
        bookService.save(book);
        return "redirect:/books";
    }

    @PostMapping("/{id}/delete")
    public String delete(@PathVariable Long id, RedirectAttributes ra) {
        try {
            bookService.deleteById(id);
            ra.addFlashAttribute("message", "Đã xóa sách");
        } catch (org.springframework.dao.DataIntegrityViolationException ex) {
            ra.addFlashAttribute("error", "Không thể xóa vì sách đang được tham chiếu (ví dụ: đánh giá/đơn hàng). Hãy gỡ liên quan trước.");
        } catch (Exception ex) {
            ra.addFlashAttribute("error", "Xóa sách thất bại: " + ex.getMessage());
        }
        return "redirect:/books";
    }

    @GetMapping("/{id}/detail")
    public String detail(@PathVariable Long id, Model model, Authentication auth) {
        Book book;
        try {
            book = bookService.findById(id);
        } catch (IllegalArgumentException ex) {
            // Sách đã bị gỡ hoặc không tồn tại: trả trang 404 thân thiện
            model.addAttribute("pageTitle", "Sách không còn tồn tại");
            model.addAttribute("notFoundMessage", "Quyển sách bạn đang xem đã bị gỡ khỏi cửa hàng.");
            var suggestions = bookService.latest(6).stream()
                    .map(b -> {
                        b.setCoverUrl(normalizeCoverUrl(b.getCoverUrl()));
                        return b;
                    })
                    .toList();
            model.addAttribute("suggestions", suggestions);
            model.addAttribute("content", "error/404 :: content");
            return "layout/layout";
        }
        // Bảo đảm coverUrl hiển thị đúng trong view và có fallback
        String normalized = normalizeCoverUrl(book.getCoverUrl());
        book.setCoverUrl(normalized != null ? normalized : "/images/sample/default.png");

        model.addAttribute("pageTitle", "Chi tiết sách");
        model.addAttribute("book", book);
        model.addAttribute("reviews", reviewService.listByBook(id));
        model.addAttribute("avgRating", reviewService.averageRating(id));
        var relatedBooks = bookService.related(id, book.getCategory() != null ? book.getCategory().getId() : null, 4)
                .stream()
                .peek(b -> b.setCoverUrl(normalizeCoverUrl(b.getCoverUrl())))
                .toList();
        model.addAttribute("relatedBooks", relatedBooks);
        boolean wished = auth != null && wishlistService.exists(id, auth.getName());
        model.addAttribute("wished", wished);
        model.addAttribute("content", "books/detail :: content");
        return "layout/layout";
    }

    @GetMapping("/api-view")
    public String apiView(Model model) {
        model.addAttribute("pageTitle", "Danh sách sách (API)");
        model.addAttribute("content", "books/api_view :: content");
        return "layout/layout";
    }

    private String normalizeCoverUrl(String raw) {
        if (raw == null) return null;
        String trimmed = raw.trim();
        if (trimmed.isEmpty()) return null;
        if (trimmed.startsWith("http://") || trimmed.startsWith("https://")) {
            return trimmed;
        }
        if (!trimmed.startsWith("/")) {
            return "/" + trimmed;
        }
        return trimmed;
    }
}
