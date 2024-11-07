package monitoring.client

import io.ktor.client.*
import io.ktor.client.call.*
import io.ktor.client.engine.cio.*
import io.ktor.client.plugins.contentnegotiation.*
import io.ktor.client.request.*
import io.ktor.http.*
import io.ktor.client.plugins.*
import io.ktor.serialization.kotlinx.json.*
import io.ktor.server.application.*
import io.ktor.server.html.*
import io.ktor.server.http.content.*
import io.ktor.server.response.*
import io.ktor.server.routing.*
import kotlinx.html.*
import monitoring.entities.Function

fun Application.clientRoutes() {
    val client = HttpClient(CIO) {
        defaultRequest {
            url("http://localhost:8080")
        }

        install(ContentNegotiation) {
            json()
        }
    }
    routing {
        static("/static") {
            resources("static")
        }
        get("/sessions") {
            val sessionsGuid: MutableSet<String> = client.get("/sessions-guids").body()
            call.respondHtml {
                head {
                    title("Select session")
                    link(rel = "stylesheet", href = "/static/styles.css", type = "text/css")
                    script { src = "/static/script.js" }
                }
                body {
                    h1 { +"Select your session" }
                    input {
                        id = "search"
                        placeholder = "Session search..."
                        onInput = "filterSessions()"
                    }
                    ul {
                        id = "session-list"
                        script {
                            unsafe {
                                +"""
                                sessions = ${sessionsGuid.map { """{"guid":"$it"}""" }};
                                """
                            }
                        }
                        sessionsGuid.forEach { guid ->
                            li {
                                attributes["data-guid"] = guid
                                onClick = "window.location.href = '/sessions/$guid';"
                                +"$guid"
                            }
                        }
                    }
                }
            }
        }

        get("/sessions/{guid}") {
            val guid = call.parameters["guid"]
            if (guid != null) {
                val uniqueMetricNames: Set<String> = client.get("/unique-metrics/$guid").body()
                val allFunctions: List<Function> = client.get("/functions/$guid").body()

                call.respondHtml(HttpStatusCode.OK) {
                    head {
                        title("Monitoring")
                        link(rel = "stylesheet", href = "/static/styles.css", type = "text/css")
                        script { src = "/static/script.js" }
                    }
                    body {
                        h1 { +"Monitoring System" }
                        table {
                            id = "metrics-table"
                            tr {
                                th { +"Function Name" }
                                uniqueMetricNames.forEach { metricName ->
                                    th { +metricName }
                                }
                            }
                            allFunctions.forEach { f ->
                                tr {
                                    td { +f.funName }
                                    uniqueMetricNames.forEach { metricName ->
                                        td {
                                            attributes["data-metric-name"] = metricName
                                            attributes["data-function-name"] = f.funName
                                            +f.metrics.getOrDefault(metricName, "")
                                        }
                                    }
                                }
                            }
                        }

                        script {
                            unsafe {
                                +"window.guid = '$guid';"
                            }
                        }
                    }
                }
            } else {
                call.respondText("Session GUID not found", status = HttpStatusCode.BadRequest)
            }
        }
    }
}