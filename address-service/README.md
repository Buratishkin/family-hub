# address-service

Семейные адреса и категории.

## Возможности

- Хранение адресов семьи.
- Общие и семейные категории.
- Архивирование категорий.
- Внутренний API чтения адресов и категорий.
- Вспомогательное построение поисковых ссылок на карты.

## Зависимости и API

PostgreSQL, Kafka и family-service для проверки доступа.

Основные маршруты: /address/**, /category/**; внутреннее чтение: /internal/addresses/** и /internal/categories/**.

- HTTP: http://localhost:8083
- Health: GET http://localhost:8083/health
- PostgreSQL: addresshub, локальный порт 5435
- Настройки: [application.yaml](src/main/resources/application.yaml)

Пользовательские запросы отправляются через gateway на http://localhost:8082.
