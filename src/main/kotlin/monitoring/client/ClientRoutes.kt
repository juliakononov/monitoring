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
        static("/css") {
            resources("css")
        }

        get("/sessions") {
            val sessionsGuid: MutableSet<String> = client.get("/sessions-guid").body()

            call.respondHtml {
                head {
                    title("Select session")
                    link(rel="stylesheet", href="/css/styles.css", type = "text/css")
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
                        sessionsGuid.forEach { guid ->
                            li {
                                attributes["data-guid"] = guid
                                onClick = "window.location.href = '/session/$guid';"
                                +"$guid"
                            }
                        }
                    }
                    script {
                        unsafe {
                            // JavaScript для фильтрации сессий
                            +"""
                    let sessions = ${sessionsGuid.map { """{"guid":"$it"}""" }};
                    function filterSessions() {
                        const query = document.getElementById('search').value.toLowerCase();
                        const filteredSessions = sessions.filter(session => session.guid.toLowerCase().includes(query));
                        const sessionListElement = document.getElementById('session-list');
                        sessionListElement.innerHTML = '';
                        filteredSessions.forEach(session => {
                            const li = document.createElement('li');
                            li.textContent = session.guid;
                            li.setAttribute('data-guid', session.guid);
                            li.onclick = () => window.location.href = '/session/' + session.guid;
                            sessionListElement.appendChild(li);
                        });
                    }
                    """
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
                        link(href="/css/styles.css", rel="stylesheet")
                        script { src="/script.css" }
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
                                            +f.metrics.getOrDefault(metricName, "-").toString()
                                        }
                                    }
                                }
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