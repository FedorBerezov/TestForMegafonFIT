


## Установка
0. На хосте для запуска установлены docker-ce 19.+, docker-compose1.26+
1. git pull репозиторий && cd репозиторий
2. docker-compose build

## Запуск
1. docker-compose up -d
2. ./startup.sh  <- заполняет ключи в консуле
  * sus/rate/limit/rps
  * is/rate/limit/rps
  * rate_limit

Порты и учетные записи

|Сервис  | Service port | Management port | Login | Pass  |
|--------|--------------|-----------------|-------|-------|
|Zabbix  | 10051        | 80              | Admin | zabbix|
|Oracle  | 1521         |                 | system| admin |
|RabbitMQ| 5672         | 15672           | guest | guest |
|Consul  | 8500         | 8500            |       |       |
|SUS     | 8380 | | | |
|IS      | 8280 | | | |
|IMDB    | 8180 nginx  | | | |
|        | | 3301 tarantool in container  |       |       |

