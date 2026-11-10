## Изучите [README.md](.\README.md) файл и структуру проекта.

# Задание 1

Контейнерная диаграмма C4: [docs/to-be-container.puml](docs/to-be-container.puml).

# Задание 2

### 1. Proxy

Реализация находится в `src/microservices/proxy`.

- `GET /health` обслуживается самим Gateway.
- `/api/events/**` направляется в `events-service`.
- `/api/movies/**` при `GRADUAL_MIGRATION=true` направляется в `movies-service` с вероятностью `MOVIES_MIGRATION_PERCENT`, иначе в монолит.
- При `GRADUAL_MIGRATION=false` весь домен movies направляется в новый сервис.
- Остальные маршруты направляются в монолит.
- Ошибка upstream возвращается как `502`, не маскируется успешным ответом.

Пример проверки:

```bash
docker compose up --build -d
curl http://localhost:8000/health
curl http://localhost:8000/api/movies
```

Для проверки постепенной миграции меняется `MOVIES_MIGRATION_PERCENT` в `docker-compose.yml` и перезапускается только Gateway:

```bash
docker compose up --build -d proxy-service
```


### 2. Kafka
Реализация находится в `src/microservices/events`.

- `POST /api/events/movie` публикует в `movie-events`.
- `POST /api/events/user` публикует в `user-events`.
- `POST /api/events/payment` публикует в `payment-events`.
- Для каждого топика запущен consumer с группой `cinemaabyss-events`
- обработанное сообщение записывается в лог.

Результаты:
- [docs/tests.png]( docs/task2/tests.png).
- [docs/kafka-topics.png](docs/task2/kafka-topics.png).
- [docs/kafka-consumers.png](docs/task2/kafka-consumers.png).

# Задание 3

Команда начала переезд в Kubernetes для лучшего масштабирования и повышения надежности. 
Вам, как архитектору осталось самое сложное:
 - реализовать CI/CD для сборки прокси сервиса
 - реализовать необходимые конфигурационные файлы для переключения трафика.


### CI/CD

Дополнил workflows новыми сервисами и подключил actions

- [Билд образов](docs/task3/github/github-actions-docker.png).
- [Проверка API](docs/task3/github/github-actions-api-tests.png).

### Proxy в Kubernetes

Подготовил окружение через vagrant (3 виртуалки, накатил k3s, сформировал кворум), 
выполнил подключение k8s клиентом с хоста, получив конфигурацию
Поправил согласно заданию, конфигурацию деплойментов

Запустил
```bash
kubectl apply -f src/kubernetes/namespace.yaml
kubectl apply -f src/kubernetes/configmap.yaml -f src/kubernetes/secret.yaml
kubectl apply -f src/kubernetes/dockerconfigsecret.yaml
kubectl apply -f src/kubernetes/postgres-init-configmap.yaml
kubectl apply -f src/kubernetes/postgres.yaml
kubectl apply -f src/kubernetes/kafka/kafka.yaml
kubectl apply -f src/kubernetes/monolith.yaml
kubectl apply -f src/kubernetes/movies-service.yaml
kubectl apply -f src/kubernetes/events-service.yaml
kubectl apply -f src/kubernetes/proxy-service.yaml
kubectl apply -f src/kubernetes/ingress.yaml
```

Пробросил на хост: http://cinemaabyss.example.com/

Скиншоты к заданию:

- [Выполнение тестов](docs/task3/k8s/k8s-tests.png).
- [Логи сервиса по событиям](docs/task3/k8s/k8s-event-service-logs.png).


# Задание 4
обновил по заданию helm

Применил
```bash
helm install cinemaabyss .\src\kubernetes\helm --namespace cinemaabyss --create-namespace
```

При ошибках обновил
```bash
helm upgrade cinemaabyss ./src/kubernetes/helm --namespace cinemaabyss
```
Скиншоты к заданию:
- [Выполнение тестов](docs/task4/helm.png).
- [Логи сервиса по событиям](docs/task4/get-movies.png).
