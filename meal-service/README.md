# meal-service

Рецепты и планирование питания.

## Возможности

- Ингредиенты и семейные рецепты.
- Планы питания по дням.
- Расчёт необходимых ингредиентов.
- Создание задач на покупку продуктов через task-service.

## Зависимости и API

PostgreSQL, Kafka, family-service и task-service.

Основные маршруты: /ingredient/**, /recipe/**, /meal-plan/**, /food/**, /me/families/{familyId}/food/**.

- HTTP: http://localhost:8086
- Health: GET http://localhost:8086/health
- PostgreSQL: mealhub, локальный порт 5438
- Настройки: [application.yaml](src/main/resources/application.yaml)

Пользовательские запросы отправляются через gateway на http://localhost:8082.
