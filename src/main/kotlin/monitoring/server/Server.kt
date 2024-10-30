package monitoring.server

import io.ktor.server.application.*
import io.ktor.server.engine.*
import io.ktor.server.netty.*
import io.ktor.serialization.kotlinx.json.*
import io.ktor.server.plugins.contentnegotiation.*
import io.ktor.server.websocket.*
import monitoring.db.MetricStorage

val metricStorage = MetricStorage()

fun main() {
    embeddedServer(Netty, port = 8080){
        install(ContentNegotiation) {
            json() // Устанавливаем JSON-сериализацию
        }
        install(WebSockets)

        serverRoutes() // Логика обработки запросов
    }.start(wait = true)
}

