-- Fix rating column precision to allow values up to 10.00
ALTER TABLE sessions ALTER COLUMN rating DECIMAL(4,2) NULL;
