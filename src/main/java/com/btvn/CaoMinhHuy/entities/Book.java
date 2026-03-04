package com.btvn.CaoMinhHuy.entities;

import jakarta.persistence.*;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
import lombok.*;

@Entity
@Data
@NoArgsConstructor
@AllArgsConstructor
public class Book {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @NotBlank(message = "Tiêu đề không được để trống")
    private String title;

    @NotBlank(message = "Tác giả không được để trống")
    private String author;

    @NotNull(message = "Giá không được để trống")
    @Positive(message = "Giá phải > 0")
    private Double price;

    @Column(columnDefinition = "TEXT")
    private String description;

    private String coverUrl;

    @Column(nullable = false)
    private Integer stock = 0;

    @ManyToOne
    @JoinColumn(name = "category_id")
    private Category category; // có thể null nếu danh mục bị xóa

    @Column(nullable = false)
    private boolean discontinued = false; // ngừng bán
}
