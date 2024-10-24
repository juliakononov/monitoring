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

        get("/server/send-sessions-guid") {
            val sessionsGuid = metricStorage.getSessionsGuid()
            call.respond(sessionsGuid)
        }

        get("/server/send-unique-metrics/{sessionGuid}") {
            val sessionGuid = call.parameters["sessionGuid"] ?: return@get call.respondText(
                "Session ID not found",
                status = HttpStatusCode.BadRequest
            )
            val uniqueMetricNames = metricStorage.getCurrentSessionMetricNames(sessionGuid)
            call.respond(uniqueMetricNames)
        }

        get("/server/send-functions/{sessionGuid}") {
            val guid = call.parameters["sessionGuid"] ?: return@get call.respondText(
                "Session ID not found",
                status = HttpStatusCode.BadRequest
            )
            val functions = metricStorage.getFunctionsToPage(guid, 0, "")
            call.respond(functions)
        }
    }
}
