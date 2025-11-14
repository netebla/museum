# Museum Information System (Java, Spring Boot)

Проект: информационно-справочная система музея.

## Стек
- Java 17, Spring Boot 3 (Web, Data JPA, Validation, Security)
- PostgreSQL 17, Flyway миграции
- REST API + Swagger UI (`/swagger-ui.html`)
- HTML/CSS/JS + Bootstrap (простая статическая страница `/`)
- Maven, Docker, Docker Compose
- GitHub Actions CI/CD

## Запуск (локально)
1. Установите JDK 17, Maven, Docker.
2. Запустите базу и приложение в Docker:
   - `docker compose up --build`
3. Откройте:
   - Главная: http://localhost:8080/
   - Swagger UI: http://localhost:8080/swagger-ui.html

По умолчанию подключение к БД настраивается через переменные окружения:
- `DB_URL` (по умолчанию `jdbc:postgresql://localhost:5432/museum` или `jdbc:postgresql://db:5432/museum` в compose)
- `DB_USERNAME` (по умолчанию `museum`)
- `DB_PASSWORD` (по умолчанию `museum`)

## Основные эндпоинты
- `GET /api/events` — список мероприятий
- `POST /api/events` — создать мероприятие
- `PUT /api/events/{id}` — обновить
- `DELETE /api/events/{id}` — удалить
- `POST /api/events/{eventId}/tickets/purchase` — купить билет (транзакционно уменьшает доступность)
- `GET /api/exhibitions` — список экспозиций (CRUD аналогично)
- `GET /api/faq` — список FAQ (CRUD аналогично)

Сейчас безопасность открыта для упрощения разработки (все запросы разрешены). На этапе доработки будет включен доступ по ролям.

## Миграции БД
Миграции Flyway находятся в `src/main/resources/db/migration`. При старте приложения автоматически создаются таблицы.

## CI
- `.github/workflows/ci.yml` — сборка Maven, тесты на каждое изменение.

## CD (пример)
- `.github/workflows/deploy.yml` — сборка Docker-образа и публикация в GHCR, затем деплой на удалённую ВМ по SSH.
- Необходимые секреты в GitHub репозитории (Settings → Secrets and variables → Actions):
  - `SSH_HOST`, `SSH_USER`, `SSH_KEY` — доступ по SSH на ВМ (приватный ключ)
  - `PROD_DB_URL`, `PROD_DB_USERNAME`, `PROD_DB_PASSWORD` — параметры БД на прод‑ВМ
  - `GITHUB_TOKEN` — добавляется автоматически; workflow уже запрошены `packages: write`

Скрипт деплоя на ВМ:
- Логин в GHCR, `docker pull ghcr.io/<owner>/museum:latest`
- Рестарт контейнера `museum` с пробросом порта 8080 и переменными окружения БД из секретов.

## Настройка репозитория и секретов (шаги)
1. Создайте пустой репозиторий на GitHub (например, `museum`).
2. Локально инициализируйте git и отправьте код:
   - `git init`
   - `git add . && git commit -m "init museum project"`
   - `git branch -M main`
   - `git remote add origin git@github.com:<YOUR_USER>/museum.git`
   - `git push -u origin main`
3. Включите Actions: Settings → Actions → General → разрешите Workflows.
4. GHCR (GitHub Container Registry): Settings → Packages → убедитесь, что включён доступ (для приватных — дайте право читать образ ВМ/юзеру).
5. Добавьте секреты (Settings → Secrets and variables → Actions → New repository secret):
   - `SSH_HOST` — IP/домен ВМ
   - `SSH_USER` — пользователь на ВМ (имеющий доступ к docker)
   - `SSH_KEY` — приватный ключ для входа по SSH (ed25519 или rsa).
     - Создать на ВМ: `ssh-keygen -t ed25519 -C "github-deploy"`
     - Добавить публичный ключ в `~/.ssh/authorized_keys`
     - Приватный ключ целиком вставить в секрет `SSH_KEY` (начиная с `-----BEGIN OPENSSH PRIVATE KEY-----`).
   - `PROD_DB_URL` — например, `jdbc:postgresql://localhost:5432/museum`
   - `PROD_DB_USERNAME` — например, `museum`
   - `PROD_DB_PASSWORD` — например, `museum`
6. На ВМ установите Docker и откройте порт 8080. Убедитесь, что PostgreSQL доступен по `PROD_DB_URL`.
7. Теперь при каждом push в `main` запустится пайплайн сборки и деплоя.

## Дальнейшие шаги
- Включить Spring Security: вход, роли (`ADMIN`, `MANAGER`, `VISITOR`)
- Добавить публичные фильтры (GET — публично; изменения — только роли)
- Расширить API (поиск по дате/тематике, статистика продаж)
- Наполнить фронтенд (формы покупки, админка) или подключить шаблоны (Thymeleaf)

## Разработка
- Запуск без Docker (локально):
  - Поднимите PostgreSQL локально и укажите `DB_URL/DB_USERNAME/DB_PASSWORD`
  - `mvn spring-boot:run`
