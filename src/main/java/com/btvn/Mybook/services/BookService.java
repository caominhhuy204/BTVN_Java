package com.btvn.Mybook.services;

import com.btvn.Mybook.entities.Book;

import java.util.List;

public interface BookService {
    List<Book> findAll();
    Book findById(Long id);
    Book save(Book book);
    void deleteById(Long id);
    List<Book> search(String keyword);
}