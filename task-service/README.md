# task-service

Семейные задачи.

## Возможности

- Создание, изменение и удаление задач.
- Исполнители, статусы и привязка к адресу.
- Повторяющиеся задачи и управление сериями.
- Текущие и старые задачи.

## Зависимости и API

PostgreSQL, Kafka, family-service и address-service. Для проверок используются локальные проекции и внутренний HTTP fallback.

Основные маршруты: /task/**, /task-recurrence-series/**.

- HTTP: http://localhost:8081
- Health: GET http://localhost:8081/health
- PostgreSQL: taskhub, локальный порт 5434
- Настройки: [application.yaml](src/main/resources/application.yaml)

Пользовательские запросы отправляются через gateway на http://localhost:8082.
