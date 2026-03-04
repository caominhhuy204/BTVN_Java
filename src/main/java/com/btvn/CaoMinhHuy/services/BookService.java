package com.btvn.CaoMinhHuy.services;

import com.btvn.CaoMinhHuy.entities.Book;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

import java.util.List;

public interface BookService {
    List<Book> findAll();
    Book findById(Long id);
    Book save(Book book);
    void deleteById(Long id);
    Page<Book> search(String keyword, Long categoryId, Double minPrice, Double maxPrice, Pageable pageable);
    List<Book> related(Long bookId, Long categoryId, int limit);
    List<Book> latest(int limit);
}
