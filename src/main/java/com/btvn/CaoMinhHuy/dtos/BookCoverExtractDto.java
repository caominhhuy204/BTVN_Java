package com.btvn.CaoMinhHuy.dtos;

public record BookCoverExtractDto(
        String title,
        String author
) {
    public static BookCoverExtractDto empty() {
        return new BookCoverExtractDto(null, null);
    }
}
