-- Optional demo influencers, useful for first-run exploration.
-- Safe to delete or skip in production (remove this file before deploying
-- if you do not want sample data).
INSERT INTO influencers (name, code, instagram_url, active) VALUES
    ('Anna', 'anna', 'https://instagram.com/shop_anna', TRUE),
    ('Max', 'max', 'https://instagram.com/shop_max', TRUE),
    ('Oleg', 'oleg', 'https://instagram.com/shop_oleg', TRUE)
ON CONFLICT (code) DO NOTHING;
