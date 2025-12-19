# Инструкция по добавлению секретов в GitHub

## Как добавить/обновить секреты в GitHub Actions

### Шаг 1: Откройте настройки репозитория
1. Перейдите в ваш репозиторий на GitHub
2. Нажмите на вкладку **Settings** (в верхнем меню репозитория)
3. В левом меню выберите **Secrets and variables** → **Actions**

### Шаг 2: Добавьте новые секреты
Нажмите кнопку **New repository secret** для каждого секрета:

#### Обязательные секреты для базы данных:
- **Name:** `PROD_DB_URL`
  - **Secret:** `jdbc:postgresql://localhost:5432/museum` (или ваш URL БД на продакшене)

- **Name:** `PROD_DB_USERNAME`
  - **Secret:** `museum` (или ваш username БД)

- **Name:** `PROD_DB_PASSWORD`
  - **Secret:** ваш пароль от базы данных

#### Обязательные секреты для SSH деплоя:
- **Name:** `SSH_HOST`
  - **Secret:** IP адрес или домен вашей ВМ (например, `123.45.67.89` или `kononovmuseum.ru`)

- **Name:** `SSH_USER`
  - **Secret:** имя пользователя на ВМ (например, `ubuntu` или `root`)

- **Name:** `SSH_KEY`
  - **Secret:** приватный SSH ключ целиком (начинается с `-----BEGIN OPENSSH PRIVATE KEY-----`)

#### Секреты для почты (reg.ru):

**Важно:** Минимально необходимый секрет — это `MAIL_PASSWORD`. Остальные можно не добавлять, если они совпадают со значениями по умолчанию.

- **Name:** `MAIL_PASSWORD`
  - **Secret:** пароль от почтового ящика `no-reply@kononovmuseum.ru` в reg.ru
  - ⚠️ **Обязательно!** Без этого секрета отправка почты не будет работать

- **Name:** `MAIL_HOST` (опционально, по умолчанию: `mail.kononovmuseum.ru`)
  - **Secret:** `mail.kononovmuseum.ru`

- **Name:** `MAIL_PORT` (опционально, по умолчанию: `587`)
  - **Secret:** `587`

- **Name:** `MAIL_USERNAME` (опционально, по умолчанию: `no-reply@kononovmuseum.ru`)
  - **Secret:** `no-reply@kononovmuseum.ru`

- **Name:** `MAIL_SMTP_AUTH` (опционально, по умолчанию: `true`)
  - **Secret:** `true`

- **Name:** `MAIL_SMTP_STARTTLS` (опционально, по умолчанию: `true`)
  - **Secret:** `true`

- **Name:** `NOTIFY_FROM_EMAIL` (опционально, по умолчанию: `no-reply@kononovmuseum.ru`)
  - **Secret:** `no-reply@kononovmuseum.ru`

### Шаг 3: Обновление существующих секретов
Если секрет уже существует и нужно его обновить:
1. Найдите секрет в списке
2. Нажмите на него
3. Нажмите **Update** (или удалите и создайте заново)
4. Введите новое значение
5. Нажмите **Update secret**

### Шаг 4: Проверка
После добавления секретов:
1. Сделайте push в ветку `main`
2. GitHub Actions автоматически запустит workflow
3. Проверьте логи деплоя в разделе **Actions** вашего репозитория

## Минимальный набор секретов для работы

Для базовой работы приложения нужны:
- ✅ `PROD_DB_URL`
- ✅ `PROD_DB_USERNAME`
- ✅ `PROD_DB_PASSWORD`
- ✅ `SSH_HOST`
- ✅ `SSH_USER`
- ✅ `SSH_KEY`
- ✅ `MAIL_PASSWORD` (для отправки уведомлений)

Остальные секреты для почты можно не добавлять, если они совпадают со значениями по умолчанию в `application.yml`.

## Решение проблем с почтой

### Проблема: SSL сертификат не соответствует домену
Если в логах появляется ошибка `SSLHandshakeException: No subject alternative DNS name matching mail.kononovmuseum.ru found`:
- В конфигурации уже отключена проверка SSL сертификата (`ssl.checkserveridentity: false`)
- Это безопасно для внутреннего использования
- Для продакшена рекомендуется настроить правильные SSL сертификаты в reg.ru

### Проблема: Письма не отправляются
1. Проверьте логи: `docker logs museum | grep -i mail`
2. Убедитесь, что `MAIL_PASSWORD` указан в секретах GitHub
3. Проверьте, что почтовый ящик создан в reg.ru
4. Убедитесь, что SMTP отправка включена в настройках reg.ru

### Просмотр логов
```bash
# Все логи
docker logs museum

# В реальном времени
docker logs -f museum

# Последние 100 строк
docker logs --tail 100 museum

# Поиск ошибок почты
docker logs museum | grep -i "mail\|email\|ticket"
```

## Безопасность

⚠️ **Важно:**
- Секреты никогда не отображаются в логах GitHub Actions
- После сохранения секрета его значение нельзя просмотреть (только обновить)
- Секреты доступны только в workflow файлах через `${{ secrets.SECRET_NAME }}`
- Не коммитьте секреты в код!

