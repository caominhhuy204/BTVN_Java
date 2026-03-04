-- Ensure rating column allows NULL and has safe constraint
ALTER TABLE review
    MODIFY rating INT NULL;

ALTER TABLE review
    DROP CHECK IF EXISTS review_rating_chk;

ALTER TABLE review
    ADD CONSTRAINT review_rating_chk CHECK (rating IS NULL OR (rating BETWEEN 1 AND 5));
