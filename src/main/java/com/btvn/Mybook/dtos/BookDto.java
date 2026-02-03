package com.btvn.Mybook.dtos;

import com.btvn.Mybook.entities.Book;
import lombok.*;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class BookDto {
    private Long id;
    private String title;
    private String author;
    private Double price;
    private String categoryName;

    public static BookDto from(Book b) {
        return new BookDto(
                b.getId(),
                b.getTitle(),
                b.getAuthor(),
                b.getPrice(),
                b.getCategory() != null ? b.getCategory().getName() : null
        );
    }
}