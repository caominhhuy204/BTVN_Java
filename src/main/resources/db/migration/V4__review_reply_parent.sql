-- Support nested replies for reviews
ALTER TABLE review
    ADD COLUMN IF NOT EXISTS parent_id BIGINT NULL,
    ADD CONSTRAINT fk_review_parent FOREIGN KEY (parent_id) REFERENCES review (id);
