package monitoring.server

import io.ktor.http.*
import io.ktor.server.application.*
import io.ktor.server.request.*
import io.ktor.server.response.*
import io.ktor.server.routing.*
import monitoring.entities.Metric

fun Application.serverRoutes() {
    routing {
        post("/server/save-metric") {
            // Получение массива Metric из запроса и их десериализация
            val metrics = call.receive<List<Metric>>()

            metricStorage.saveMetrics(metrics)
            call.respond(HttpStatusCode.OK, "Successfully processed and saved ${metrics.size} metrics")
        }

        //TODO: переписать
//        post("/server/new_session") {
//            // Обработка новой "сессии"
//            // TODO: Проверить работу функции + добавить в метрики ID
//
//            val new = call.receive<String>()
//            if (new == "New") {
//                val myUuid = UUID.randomUUID()
//                call.respond(myUuid)
//            }
//        }

        get("/server/send-unique-metrics") {
            val uniqueMetricNames = metricStorage.getUniqueMetricNames()
            call.respond(uniqueMetricNames)
        }

        get("/server/send-functions") {
            val functions = metricStorage.getFunctionsToPage(0, "")
            call.respond(functions)
        }
    }
}
