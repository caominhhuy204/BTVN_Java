-- Force rating column to allow NULL to support optional scores
ALTER TABLE review
    MODIFY rating INT NULL;
