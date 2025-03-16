# Core Service

Spring Boot приложение реализующий бизнес логику пользователей, запускаемое через Docker Compose.

## Требования

- Docker (с Docker Compose)
- Java 17 (опционально)
- Maven (опционально)

## Конфигурация

1. Создайте `.env.properties` файл с переменными `.env.properties.example`:
2. Создайте `.env`:

Пример:
```env
DATASOURCE_USERNAME=postgres
DATASOURCE_PASSWORD=postgres
JWT_SECRET===========secret=phrase=string=example=============
YANDEX_CLIENT_ID={client id from https://oauth.yandex.ru/}
YANDEX_CLIENT_SECRET={client secret from https://oauth.yandex.ru/}
STATIC_URL=http://localhost:8080/swagger-ui/index.html
```

## Запуск

### Docker Compose
```bash
docker-compose up --build -d
```
