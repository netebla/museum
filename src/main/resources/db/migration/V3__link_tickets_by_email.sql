-- Связываем существующие билеты с пользователями по email
-- Обновляем user_id для билетов, у которых user_id NULL, но buyer_email совпадает с email пользователя
UPDATE tickets t
SET user_id = u.id
FROM users u
WHERE t.user_id IS NULL
  AND t.buyer_email IS NOT NULL
  AND u.email IS NOT NULL
  AND LOWER(TRIM(t.buyer_email)) = LOWER(TRIM(u.email));


