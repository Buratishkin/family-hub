# family-service

Семьи и совместное планирование.

## Возможности

- Семьи, участники, роли и приглашения.
- Персональные псевдонимы участников.
- Семейные опросы.
- Календарь занятости с проверкой пересечений.
- Внутренний API проверки доступа к семье.

## Зависимости и API

PostgreSQL и Kafka; сведения о пользователях поступают через события.

Основные маршруты: /family/**, /invite/**, /alias/**, /me/families/{familyId}/polls/**, /me/families/{familyId}/plans/**.

- HTTP: http://localhost:8084
- Health: GET http://localhost:8084/health
- PostgreSQL: familyservice, локальный порт 5436
- Настройки: [application.yaml](src/main/resources/application.yaml)

Пользовательские запросы отправляются через gateway на http://localhost:8082.
