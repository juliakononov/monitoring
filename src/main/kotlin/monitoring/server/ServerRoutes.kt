package monitoring.server

import io.ktor.http.*
import io.ktor.server.application.*
import io.ktor.server.request.*
import io.ktor.server.response.*
import io.ktor.server.routing.*
import io.ktor.websocket.*
import monitoring.entities.Metric
import java.util.*


fun Application.serverRoutes() {
    routing {
        post("/metrics") {
            val metrics = call.receive<List<Metric>>()

            metricStorage.saveMetrics(metrics)

            metrics.forEach { metric ->
                val sessionGuid = metric.guid
                val message = """
                    {
                        "funName": "${metric.params.funName}",
                        "metricName": "${metric.name}",
                        "value": "${metric.params.value}"
                    }
                """.trimIndent()

                webSocketSessions[sessionGuid]?.forEach { session ->
                    session.send(Frame.Text(message))
                }
            }

            call.respond(HttpStatusCode.OK, "Successfully processed and saved ${metrics.size} metrics")
        }

        get("/new-session") {
            val myUuid = UUID.randomUUID().toString()
            call.respond(HttpStatusCode.Created, myUuid)
        }

        get("/sessions-guids") {
            val sessionsGuid = metricStorage.getSessionsGuid()
            call.respond(sessionsGuid)
        }

        get("/unique-metrics/{sessionGuid}") {
            val sessionGuid = call.parameters["sessionGuid"] ?: return@get call.respondText(
                "Session ID not found",
                status = HttpStatusCode.BadRequest
            )
            val uniqueMetricNames = metricStorage.getCurrentSessionMetricNames(sessionGuid)
            call.respond(uniqueMetricNames)
        }

        get("/functions/{sessionGuid}") {
            val guid = call.parameters["sessionGuid"] ?: return@get call.respondText(
                "Session ID not found",
                status = HttpStatusCode.BadRequest
            )
            val functions = metricStorage.getFunctionsToPage(guid, 0, "")
            call.respond(functions)
        }
    }
}

