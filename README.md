# FamilyHub

Backend приложения для совместного планирования семейных дел: задачи, питание, адреса, опросы, календарь и уведомления.

## Возможности

- Регистрация и вход с JWT access/refresh tokens.
- Семьи, участники, приглашения и проверка доступа.
- Задачи с исполнителями, статусами и повторениями.
- Семейный календарь с проверкой пересечений занятости.
- Адреса, категории, рецепты и планы питания.
- История уведомлений и поддержка push через Firebase.

## Стек

Java 21, Spring Boot, Spring Security, Spring Data JPA, PostgreSQL, Apache Kafka, Flyway, Maven, Docker Compose.

## Приложения

| Приложение | Назначение | HTTP-порт |
|---|---|---:|
| [gateway](gateway/README.md) | Единая точка входа | 8082 |
| [auth-service](auth-service/README.md) | Регистрация, вход и токены | 8085 |
| [family-service](family-service/README.md) | Семьи, участники, опросы и календарь | 8084 |
| [task-service](task-service/README.md) | Задачи и повторяющиеся серии | 8081 |
| [address-service](address-service/README.md) | Адреса и категории | 8083 |
| [meal-service](meal-service/README.md) | Рецепты и питание | 8086 |
| [notification-service](notification-service/README.md) | Уведомления, устройства и push | 8087 |
| Корневое приложение, src/ | BFF: собирает данные для экранов /me/** | 8080 |

Клиент обращается к gateway. Сервисы используют отдельные PostgreSQL-базы и взаимодействуют через HTTP и Kafka. Для доменных событий используются outbox и защита от повторной обработки. Миграции БД выполняются через Flyway.

Подробная документация — в [technical-docs](technical-docs/).