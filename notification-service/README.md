# notification-service

Уведомления и push-доставка.

## Возможности

- Персональные уведомления по событиям Kafka.
- История и отметки прочтения.
- Регистрация и отключение устройств.
- Очередь доставки и повторные попытки.
- Logging и Firebase Cloud Messaging adapters.

## Зависимости и API

PostgreSQL и Kafka. Получатели определяются по локальным проекциям пользователей и участников.

Основные маршруты: /notifications, /notification-devices.

- HTTP: http://localhost:8087
- Health: GET http://localhost:8087/health
- PostgreSQL: notificationhub, локальный порт 5439
- Настройки: [application.yaml](src/main/resources/application.yaml)

Пользовательские запросы отправляются через gateway на http://localhost:8082.

По умолчанию выбран logging: реальный push не отправляется. Для Firebase задайте FAMILYHUB_NOTIFICATION_PUSH_PROVIDER=firebase и FIREBASE_CREDENTIALS_PATH с путём к service-account JSON. История сохраняется и без устройств; отправка push не означает прочтение.
