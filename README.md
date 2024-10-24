# Мониторинг Система

## Сборка
Cоздание Jar-архивов:
```bash
./gradlew buildAllJars
```
---
## Запуск
Запуск сервера:
```bash
java -jar build/libs/monitoring-0.0.1-server.jar   
```

Запуск клиента:
```bash
java -jar build/libs/monitoring-0.0.1-client.jar   
```
---
## Описание
После запуска сервер сможет генерировать и отправлять агенту уникальный id для сессии по GET-запросу:
* `http://<YOUR-SERVER-HOST>/server/new-session`

Полученный id важно сохранить, для маркировки дальнейших метрик. 

После этого агент может начать собирать метрики и отправлять их с помощью POST-запроса по URL-адресу:
* `http://<YOUR-SERVER-HOST>/server/save-metric`
---
После запуска клиента получить список всех сессий можно по адресу:
* `http://<YOUR-CLIENT-HOST>/client/sessions`

## Формат
Запросы принимаются в виде json-файла со следующей структурой:

```json
[
    {
        "guid": "1234"
        "name": "MetricName",
        "params": {
            "funName": "name",
            "type": "<type>", 
            "value": "<some value of the type>",
            "transitive": false
        }
    }
]
```
* Метрик может быть много
  ```json
    [
      {
          "guid": "1234"
          "name": "Metric1",
          "params": {...}
      },
      <...>
      {
          "guid": "1234"
          "name": "MetricN",
          "params": {...}
      }
    ]
    ```
* `type` может принимать значения:
  * `Int`
  * `String`
  * `Boolean`
  * `Double`
* `value` должно соответствовать `type`
* `transitive` может быть:
  * `true` 
  * `false`
---
