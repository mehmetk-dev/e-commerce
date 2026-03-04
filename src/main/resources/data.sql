-- Fix null values for new columns (safe: does nothing if column doesn't exist yet)
-- These run AFTER Hibernate ddl-auto=update has created/altered the columns

-- Seed categories (only if table is empty)
INSERT INTO categories (name, description, deleted)
SELECT 'Mobilya', 'Antika masa, sandalye, dolap, koltuk ve diğer mobilya eserleri', false
WHERE NOT EXISTS (SELECT 1 FROM categories LIMIT 1);

INSERT INTO categories (name, description, deleted)
SELECT 'Porselen & Seramik', 'Osmanlı, Avrupa ve Uzak Doğu porselenleri, seramik eserler', false
WHERE NOT EXISTS (SELECT 1 FROM categories WHERE name = 'Porselen & Seramik');

INSERT INTO categories (name, description, deleted)
SELECT 'Saatler', 'Antika duvar saatleri, masa saatleri, cep saatleri', false
WHERE NOT EXISTS (SELECT 1 FROM categories WHERE name = 'Saatler');

INSERT INTO categories (name, description, deleted)
SELECT 'Halı & Kilim', 'El dokuması antika halılar, kilimler ve tekstil eserler', false
WHERE NOT EXISTS (SELECT 1 FROM categories WHERE name = 'Halı & Kilim');

INSERT INTO categories (name, description, deleted)
SELECT 'Aydınlatma', 'Avizeler, aplikler, masa lambaları ve antik aydınlatma', false
WHERE NOT EXISTS (SELECT 1 FROM categories WHERE name = 'Aydınlatma');

INSERT INTO categories (name, description, deleted)
SELECT 'Gümüş & Metal', 'Osmanlı gümüşleri, bakır eserler, bronz heykeller', false
WHERE NOT EXISTS (SELECT 1 FROM categories WHERE name = 'Gümüş & Metal');

INSERT INTO categories (name, description, deleted)
SELECT 'Tablo & Resim', 'Yağlı boya tablolar, gravürler, baskı eserler', false
WHERE NOT EXISTS (SELECT 1 FROM categories WHERE name = 'Tablo & Resim');

INSERT INTO categories (name, description, deleted)
SELECT 'Aksesuar & Dekor', 'Antika aynalar, vazolar, kutular, dekoratif objeler', false
WHERE NOT EXISTS (SELECT 1 FROM categories WHERE name = 'Aksesuar & Dekor');

INSERT INTO categories (name, description, deleted)
SELECT 'Mücevher', 'Antika takılar, broşlar, kolyeler, yüzükler', false
WHERE NOT EXISTS (SELECT 1 FROM categories WHERE name = 'Mücevher');

INSERT INTO categories (name, description, deleted)
SELECT 'Kitap & Belge', 'Eski kitaplar, haritalar, el yazmaları, fermanlar', false
WHERE NOT EXISTS (SELECT 1 FROM categories WHERE name = 'Kitap & Belge');
