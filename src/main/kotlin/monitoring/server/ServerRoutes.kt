package monitoring.server

import io.ktor.http.*
import io.ktor.server.application.*
import io.ktor.server.request.*
import io.ktor.server.response.*
import io.ktor.server.routing.*
import monitoring.entities.Metric
import java.util.*

fun Application.serverRoutes() {
    routing {
        post("/server/save-metric") {
            // Получение массива Metric из запроса и их десериализация
            val metrics = call.receive<List<Metric>>()

            metricStorage.saveMetrics(metrics)
            call.respond(HttpStatusCode.OK, "Successfully processed and saved ${metrics.size} metrics")
        }

        get("/server/new-session") {
            val myUuid = UUID.randomUUID().toString()
            call.respond(HttpStatusCode.Created, myUuid)
        }

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
