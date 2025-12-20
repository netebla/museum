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
   - Swagger UI: http://localhost:8080/swagger-ui.html (требуется авторизация)
   - Админ-панель: http://localhost:8080/admin (требуется авторизация, логин: `admin`, пароль: `admin`)

По умолчанию подключение к БД настраивается через переменные окружения:
- `DB_URL` (по умолчанию `jdbc:postgresql://localhost:5432/museum` или `jdbc:postgresql://db:5432/museum` в compose)
- `DB_USERNAME` (по умолчанию `museum`)
- `DB_PASSWORD` (по умолчанию `museum`)

Настройка почты (для отправки уведомлений о заявках на билеты):
- `MAIL_HOST` (по умолчанию `mail.kononovmuseum.ru`)
- `MAIL_PORT` (по умолчанию `587`)
- `MAIL_USERNAME` (по умолчанию `no-reply@kononovmuseum.ru`)
- `MAIL_PASSWORD` — пароль от почтового ящика (обязательно указать)
- `MAIL_SMTP_AUTH` (по умолчанию `true`)
- `MAIL_SMTP_STARTTLS` (по умолчанию `true`)
- `NOTIFY_FROM_EMAIL` (по умолчанию `no-reply@kononovmuseum.ru`)

**Важно:** Для работы отправки почты необходимо:
1. Создать почтовый ящик `no-reply@kononovmuseum.ru` в панели управления reg.ru
2. Указать пароль от этого ящика в переменной окружения `MAIL_PASSWORD`
3. Убедиться, что SMTP отправка включена в настройках почты reg.ru

**Примечание:** В конфигурации отключена проверка SSL сертификата (`ssl.checkserveridentity: false`) для совместимости с почтовыми серверами reg.ru. Это безопасно для внутреннего использования, но для продакшена рекомендуется настроить правильные SSL сертификаты.

## Основные эндпоинты

### Публичные эндпоинты (доступны без авторизации):
- `GET /api/events` — список мероприятий
- `GET /api/events/{id}` — получить мероприятие
- `GET /api/exhibitions` — список экспозиций
- `GET /api/exhibitions/{id}` — получить экспозицию
- `GET /api/faq` — список FAQ
- `GET /api/faq/{id}` — получить вопрос/ответ
- `POST /api/events/{eventId}/tickets/purchase` — купить билет (транзакционно уменьшает доступность, автоматически отправляет email-уведомление)

### Эндпоинты для авторизованных пользователей:
- `GET /api/me` — информация о текущем пользователе
- `GET /profile` — личный кабинет пользователя (отображает билеты пользователя)

### Эндпоинты для ролей ADMIN/MANAGER:
- `POST /api/events` — создать мероприятие
- `PUT /api/events/{id}` — обновить мероприятие
- `POST /api/exhibitions` — создать экспозицию
- `PUT /api/exhibitions/{id}` — обновить экспозицию
- `POST /api/faq` — создать вопрос/ответ
- `PUT /api/faq/{id}` — обновить вопрос/ответ

### Эндпоинты только для роли ADMIN:
- `DELETE /api/events/{id}` — удалить мероприятие
- `DELETE /api/exhibitions/{id}` — удалить экспозицию
- `DELETE /api/faq/{id}` — удалить вопрос/ответ

**Функционал уведомлений:**
- При покупке билета автоматически отправляется email-уведомление на адрес покупателя
- Уведомление содержит информацию о мероприятии, датах, зале и номере заявки
- Ошибки отправки почты логируются, но не блокируют создание билета

## Безопасность и роли

Система использует Spring Security с разграничением доступа по ролям:

- **VISITOR** — обычный пользователь, может просматривать публичные страницы и покупать билеты
- **MANAGER** — может создавать и редактировать мероприятия, экспозиции, FAQ через API и админ-панель
- **ADMIN** — полный доступ, включая удаление записей и просмотр статистики

**Публичные ресурсы:**
- Все статические страницы (`/`, `/events.html`, `/exhibitions.html`, `/tickets.html`, `/visit.html`, `/about.html`, `/author.html`)
- GET-запросы к API (чтение данных)
- Покупка билетов (`POST /api/events/{eventId}/tickets/purchase`)

**Требуется авторизация:**
- Swagger UI (`/swagger-ui.html`)
- Личный кабинет (`/profile`)
- API эндпоинт `/api/me`

**Требуются роли ADMIN/MANAGER:**
- Админ-панель (`/admin/**`)
- Создание и редактирование через API (`POST`, `PUT /api/**`)

**Требуется роль ADMIN:**
- Удаление через API (`DELETE /api/**`)
- Статистика (`/admin/stats`)
- Удаление через веб-интерфейс (`POST /admin/**/delete`)

**Дефолтный администратор:** при первом запуске создаётся пользователь `admin` с паролем `admin` (роль ADMIN).

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
   - **Секреты для почты (reg.ru):**
     - `MAIL_HOST` — `mail.kononovmuseum.ru` (или оставьте пустым, используется значение по умолчанию)
     - `MAIL_PORT` — `587` (или оставьте пустым, используется значение по умолчанию)
     - `MAIL_USERNAME` — `no-reply@kononovmuseum.ru` (или оставьте пустым, используется значение по умолчанию)
     - `MAIL_PASSWORD` — **обязательно** пароль от почтового ящика `no-reply@kononovmuseum.ru`
     - `MAIL_SMTP_AUTH` — `true` (или оставьте пустым, используется значение по умолчанию)
     - `MAIL_SMTP_STARTTLS` — `true` (или оставьте пустым, используется значение по умолчанию)
     - `NOTIFY_FROM_EMAIL` — `no-reply@kononovmuseum.ru` (или оставьте пустым, используется значение по умолчанию)
6. На ВМ установите Docker и откройте порт 8080. Убедитесь, что PostgreSQL доступен по `PROD_DB_URL`.
7. Теперь при каждом push в `main` запустится пайплайн сборки и деплоя.

## Дальнейшие шаги
- Расширить API (поиск по дате/тематике)
- Добавить управление FAQ через админ-панель
- Улучшить UX фронтенда

## Реализованные функции
- **Статистика продаж** — доступна в админ-панели (`/admin/stats`):
  - отображение количества зарегистрированных пользователей;
  - отображение количества заявок на билеты;
  - статистика по мероприятиям и экспозициям;
  - топ мероприятий по проданным билетам.

- **Сохранение паролей в браузере:**
  - формы входа и регистрации настроены для работы с менеджерами паролей браузера (Safari, Chrome, Firefox и др.);
  - используются стандартные атрибуты HTML `autocomplete` для корректного распознавания полей;
  - браузеры автоматически предлагают сохранить пароль после успешного входа;
  - при следующем посещении браузер предлагает автозаполнение сохраненных данных.

## Технические детали

### Компиляция
- В `pom.xml` настроен флаг `-parameters` для компилятора Maven, что позволяет Spring правильно определять имена параметров методов в `@PathVariable` и `@RequestParam` без явного указания имен.

### Обработка ошибок
- Формы на фронтенде показывают детальные сообщения об ошибках от сервера
- Валидация данных выполняется как на клиенте (JavaScript), так и на сервере (Spring Validation)
- Ошибки отправки почты логируются, но не прерывают процесс создания билета

### Логирование
- Логи приложения можно просмотреть через `docker logs museum`
- Для отслеживания в реальном времени: `docker logs -f museum`
- Последние N строк: `docker logs --tail 100 museum`
- С временными метками: `docker logs -t museum`
- Поиск ошибок почты: `docker logs museum | grep -i "mail\|email\|ticket"`
- Ошибки отправки почты логируются с уровнем WARN

## Разработка
- Запуск без Docker (локально):
  - Поднимите PostgreSQL локально и укажите `DB_URL/DB_USERNAME/DB_PASSWORD`
  - `mvn spring-boot:run`
