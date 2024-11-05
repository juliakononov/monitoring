package monitoring.server

import io.ktor.serialization.kotlinx.*
import io.ktor.server.application.*
import io.ktor.server.routing.*
import io.ktor.server.websocket.*
import io.ktor.websocket.*
import kotlinx.coroutines.channels.consumeEach
import kotlinx.serialization.json.Json
import java.time.Duration


fun Application.configureSockets() {
    install(WebSockets) {
        contentConverter = KotlinxWebsocketSerializationConverter(Json)
        pingPeriod = Duration.ofSeconds(15)
        timeout = Duration.ofSeconds(15)
        maxFrameSize = Long.MAX_VALUE
        masking = false
    }

    routing {
        webSocket("/updates/{guid}") {
            val sessionGuid = call.parameters["guid"]
            if (sessionGuid != null) {
                val sessions = webSocketSessions.getOrPut(sessionGuid) { mutableListOf() }
                sessions.add(this)

                try {
                    incoming.consumeEach {}
                } finally {
                    sessions.remove(this)
                }
            }
        }
    }
}
val webSocketSessions = mutableMapOf<String, MutableList<DefaultWebSocketSession>>()
