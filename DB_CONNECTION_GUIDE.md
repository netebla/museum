# Руководство по подключению к базе данных и применению миграций

## Вариант 1: Автоматическое применение через Flyway (рекомендуется)

Flyway автоматически применит новую миграцию `V4__seed_data.sql` при запуске приложения.

### Запуск приложения:
```bash
docker-compose up -d
```

После запуска миграция будет применена автоматически. Проверить можно через логи:
```bash
docker-compose logs app | grep -i flyway
```

---

## Вариант 2: Ручное подключение к контейнеру БД

### Шаг 1: Запустить контейнеры (если не запущены)
```bash
docker-compose up -d db
```

### Шаг 2: Подключиться к контейнеру PostgreSQL

**Вариант 2.1: Через docker exec (из VM)**
```bash
docker-compose exec db psql -U museum -d museum
```

**Вариант 2.2: Через psql из VM (если установлен PostgreSQL клиент)**
```bash
psql -h localhost -p 5432 -U museum -d museum
# Пароль: museum
```

### Шаг 3: Проверить текущие данные
```sql
-- Посмотреть выставки
SELECT id, title, status FROM exhibitions;

-- Посмотреть мероприятия
SELECT id, title, start_date FROM events;

-- Посмотреть FAQ
SELECT id, question, category FROM faq;
```

### Шаг 4: Если нужно применить миграцию вручную
```sql
-- Выполнить содержимое файла V4__seed_data.sql
\i /path/to/V4__seed_data.sql
```

Или скопировать SQL-запросы и выполнить их напрямую в psql.

---

## Вариант 3: Применить миграцию через docker exec (одной командой)

```bash
# Скопировать файл миграции в контейнер и выполнить
docker-compose exec -T db psql -U museum -d museum < src/main/resources/db/migration/V4__seed_data.sql
```

Или если контейнер уже запущен:
```bash
cat src/main/resources/db/migration/V4__seed_data.sql | docker-compose exec -T db psql -U museum -d museum
```

---

## Параметры подключения к БД

- **Хост**: localhost (из VM) или `db` (из контейнера app)
- **Порт**: 5432
- **База данных**: museum
- **Пользователь**: museum
- **Пароль**: museum

---

## Проверка данных после применения миграции

### Через psql:
```sql
-- Количество выставок
SELECT COUNT(*) FROM exhibitions;

-- Количество мероприятий
SELECT COUNT(*) FROM events;

-- Количество FAQ
SELECT COUNT(*) FROM faq;

-- Примеры данных
SELECT title, status FROM exhibitions LIMIT 5;
SELECT title, start_date FROM events LIMIT 5;
SELECT question FROM faq LIMIT 5;
```

### Через REST API (после запуска приложения):
```bash
# Выставки
curl http://localhost:8080/api/exhibitions

# Мероприятия
curl http://localhost:8080/api/events

# FAQ
curl http://localhost:8080/api/faq
```

---

## Полезные команды Docker

```bash
# Посмотреть логи БД
docker-compose logs db

# Остановить контейнеры
docker-compose down

# Пересоздать БД с нуля (удалит все данные!)
docker-compose down -v
docker-compose up -d
```

