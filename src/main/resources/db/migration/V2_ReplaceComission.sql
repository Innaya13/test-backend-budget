UPDATE budget SET type = 'Расход' WHERE type = 'Комиссия';
ALTER TABLE budget ALTER COLUMN type TYPE VARCHAR(10) USING type::VARCHAR(10);
