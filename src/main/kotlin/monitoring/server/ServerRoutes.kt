package monitoring.server

import io.ktor.http.*
import io.ktor.server.application.*
import io.ktor.server.request.*
import io.ktor.server.response.*
import io.ktor.server.routing.*
import io.ktor.server.websocket.*
import io.ktor.websocket.*
import monitoring.entities.Metric
import java.util.*


fun Application.serverRoutes() {
    routing {
        post("/metrics") {
            // Получение массива Metric из запроса и их десериализация
            val metrics = call.receive<List<Metric>>()

            metricStorage.saveMetrics(metrics)

            val sessionGuid = metrics.firstOrNull()?.guid
            if (sessionGuid != null) {
                val updatedMetricNames = metrics.map { it.name }.toSet()
                WebSocketManager.notifyClients(sessionGuid, updatedMetricNames)
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

        webSocket("/updates") {
            val sessionGuid = call.parameters["sessionGuid"]  // Извлечение sessionGuid из параметров URL
            if (sessionGuid != null) {
                WebSocketManager.handleClient(this, sessionGuid)
            } else {
                close(CloseReason(CloseReason.Codes.VIOLATED_POLICY, "sessionGuid is missing"))
            }
        }
    }
}


object WebSocketManager {
    private val clients = mutableMapOf<String, MutableList<DefaultWebSocketSession>>()  // [sessionGuid -> List of clients]

    suspend fun handleClient(session: DefaultWebSocketSession, sessionGuid: String) {
        // Добавляем клиента для сессии
        WebSocketManager.addClient(sessionGuid, session)

        try {
            for (frame in session.incoming) {
                // Обработка входящих сообщений (если нужно)
            }
        } catch (e: Exception) {
            e.printStackTrace()
        } finally {
            WebSocketManager.removeClient(sessionGuid, session)
        }
    }
    fun addClient(sessionGuid: String, session: DefaultWebSocketSession) {
        clients.computeIfAbsent(sessionGuid) { mutableListOf() }.add(session)
    }

    fun removeClient(sessionGuid: String, session: DefaultWebSocketSession) {
        clients[sessionGuid]?.remove(session)
    }

    suspend fun notifyClients(sessionGuid: String, updatedMetrics: Set<String>) {
        val clientsForSession = clients[sessionGuid]
        clientsForSession?.forEach { client ->
            client.send(Frame.Text(updatedMetrics.joinToString(",")))
        }
    }
}