package com.btvn.CaoMinhHuy.repositories;

import com.btvn.CaoMinhHuy.dtos.CategoryCountDto;
import com.btvn.CaoMinhHuy.entities.Book;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;

public interface BookRepository extends JpaRepository<Book, Long> {
    List<Book> findByTitleContainingIgnoreCaseOrAuthorContainingIgnoreCase(String title, String author);

    List<Book> findTop5ByOrderByIdDesc();
    List<Book> findByCategory_Id(Long categoryId);
    List<Book> findTop5ByCategory_IdAndIdNotOrderByIdDesc(Long categoryId, Long excludeId);

    @Query("select avg(b.price) from Book b")
    Double avgPrice();

    @Query("select max(b.price) from Book b")
    Double maxPrice();

    @Query("select min(b.price) from Book b")
    Double minPrice();

    @Query("""
            select new com.btvn.CaoMinhHuy.dtos.CategoryCountDto(c.name, count(b))
            from Book b
            join b.category c
            group by c.name
            order by count(b) desc
            """)
    List<CategoryCountDto> countByCategory();

    @Query("select coalesce(sum(b.price), 0) from Book b")
    Double totalRevenue();

    @Query("""
            select b from Book b
            where (:keyword is null
                or lower(b.title) like lower(concat('%', :keyword, '%'))
                or lower(b.author) like lower(concat('%', :keyword, '%')))
            and (:categoryId is null or b.category.id = :categoryId)
            and (:minPrice is null or b.price >= :minPrice)
            and (:maxPrice is null or b.price <= :maxPrice)
            and b.discontinued = false
            """)
    Page<Book> search(@Param("keyword") String keyword,
                      @Param("categoryId") Long categoryId,
                      @Param("minPrice") Double minPrice,
                      @Param("maxPrice") Double maxPrice,
                      Pageable pageable);

    long countByCategory_Id(Long categoryId);
}
