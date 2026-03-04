package com.btvn.CaoMinhHuy.services;

import com.btvn.CaoMinhHuy.entities.Book;
import com.btvn.CaoMinhHuy.entities.Category;
import com.btvn.CaoMinhHuy.repositories.BookRepository;
import com.btvn.CaoMinhHuy.repositories.CategoryRepository;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.transaction.annotation.Transactional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@SpringBootTest
@ActiveProfiles("test")
@Transactional
class CategoryServiceTest {

    @Autowired
    private CategoryService categoryService;

    @Autowired
    private CategoryRepository categoryRepository;

    @Autowired
    private BookRepository bookRepository;

    @Test
    @DisplayName("Không cho trùng tên danh mục (case-insensitive)")
    void shouldRejectDuplicateName() {
        Category c1 = categoryService.save(new Category(null, "Tech"));
        assertThat(c1.getId()).isNotNull();

        Category dup = new Category(null, "tech");
        assertThatThrownBy(() -> categoryService.save(dup))
                .isInstanceOf(IllegalStateException.class);
    }

    @Test
    @DisplayName("Xóa danh mục với chuyển sách sang danh mục đích")
    void deleteWithReassignShouldMoveBooks() {
        Category source = categoryService.save(new Category(null, "Nguon"));
        Category target = categoryService.save(new Category(null, "Dich"));

        Book book = new Book();
        book.setTitle("S1");
        book.setAuthor("A1");
        book.setPrice(10.0);
        book.setCategory(source);
        bookRepository.save(book);

        categoryService.delete(source.getId(), target.getId());

        assertThat(categoryRepository.findById(source.getId())).isEmpty();
        Book reloaded = bookRepository.findById(book.getId()).orElseThrow();
        assertThat(reloaded.getCategory().getId()).isEqualTo(target.getId());
    }

    @Test
    @DisplayName("Không cho xóa khi còn sách nếu chưa chọn danh mục đích")
    void deleteWithoutTargetShouldFailWhenHasBooks() {
        Category source = categoryService.save(new Category(null, "Nguon"));
        Book book = new Book();
        book.setTitle("S1");
        book.setAuthor("A1");
        book.setPrice(10.0);
        book.setCategory(source);
        bookRepository.save(book);

        assertThatThrownBy(() -> categoryService.delete(source.getId(), null))
                .isInstanceOf(IllegalStateException.class);
    }
}
