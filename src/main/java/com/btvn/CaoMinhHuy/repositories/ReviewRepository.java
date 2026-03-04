package com.btvn.CaoMinhHuy.repositories;

import com.btvn.CaoMinhHuy.entities.Review;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

import java.util.List;

public interface ReviewRepository extends JpaRepository<Review, Long> {
    List<Review> findByBook_IdOrderByCreatedAtDesc(Long bookId);
    List<Review> findByBook_IdAndParentIsNullOrderByCreatedAtDesc(Long bookId);
    List<Review> findByParent_IdOrderByCreatedAtAsc(Long parentId);

    @Query("select coalesce(avg(r.rating),0) from Review r where r.book.id = :bookId")
    Double averageRating(Long bookId);

    long countByBook_Id(Long bookId);

    void deleteByBook_Id(Long bookId);
}
