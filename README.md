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
После запуска сервер сможет принимать post запросы на сохранение Метрик по url адресу:

* `http://<YOUR-SERVER-HOST>/server/save-metric`

После запуска клиента получить таблицу можно по адресу:
* `http://<YOUR-CLIENT-HOST>/client/table`

## Формат
Запросы принимаются в виде json-файла со следующей структурой:

```json
[
    {
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
          "name": "Metric1",
          "params": {...}
      },
      <...>
      {
          "name": "MetricN",
          "params": {...}
      }
    ]
    ```
* `type` может принимать значения:
  * `Int`
  * `String`
  * `Boolean`
  * **TODO:** `Double`
* `value` должно соответствовать `type`
* `transitive` может быть:
  * `true` 
  * `false`
---