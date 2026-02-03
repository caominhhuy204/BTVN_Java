package com.btvn.Mybook.controllers.api;

import com.btvn.Mybook.dtos.BookDto;
import com.btvn.Mybook.services.BookService;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/books")
@CrossOrigin(origins = "*")
public class ApiBookController {

    private final BookService bookService;

    @GetMapping
    public List<BookDto> all() {
        return bookService.findAll().stream().map(BookDto::from).toList();
    }

    @GetMapping("/{id}")
    public BookDto one(@PathVariable Long id) {
        return BookDto.from(bookService.findById(id));
    }

    @DeleteMapping("/{id}")
    public void delete(@PathVariable Long id) {
        bookService.deleteById(id);
    }
}