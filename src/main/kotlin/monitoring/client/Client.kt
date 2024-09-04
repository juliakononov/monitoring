package monitoring.client

import io.ktor.server.engine.*
import io.ktor.server.netty.*

fun main() {
    embeddedServer(Netty, port = 8081){
        clientRoutes() // Логика обработки запросов
    }.start(wait = true)
}