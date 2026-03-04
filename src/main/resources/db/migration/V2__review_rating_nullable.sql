-- Allow nullable rating and relax check to accept NULL or 1..5
ALTER TABLE review
    MODIFY rating INT NULL,
    DROP CHECK IF EXISTS review_chk_1,
    ADD CONSTRAINT review_rating_chk CHECK (rating IS NULL OR (rating BETWEEN 1 AND 5));
