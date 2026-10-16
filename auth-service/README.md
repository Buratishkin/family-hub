# auth-service

Регистрация и вход пользователей.

## Возможности

- Регистрация аккаунтов и хеширование паролей.
- Вход, обновление токенов и выход.
- Выдача JWT access/refresh tokens.
- События регистрации пользователей через Kafka.

## Зависимости и API

PostgreSQL и Kafka.

Основные маршруты: /auth/**.

- HTTP: http://localhost:8085
- Health: GET http://localhost:8085/health
- PostgreSQL: authhub, локальный порт 5437
- Настройки: [application.yaml](src/main/resources/application.yaml)

Пользовательские запросы отправляются через gateway на http://localhost:8082.
