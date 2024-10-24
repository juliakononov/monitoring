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
import io.ktor.server.response.*
import io.ktor.server.routing.*
import kotlinx.html.*
import monitoring.entities.Function

const val PAGE_REFRESH_INTERVAL = 1
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
        get("client/sessions") {
            // Запрос списка сессий с другого API
            val sessionsGuid: MutableSet<String> = client.get("/server/send-sessions-guid").body()

            call.respondHtml {
                head {
                    title("Select session")
                    style {
                        +"body { font-family: Arial, sans-serif; margin: 20px; }"
                        +"input { padding: 10px; margin-bottom: 20px; font-size: 16px; }"
                        +"ul { list-style-type: none; padding: 0; }"
                        +"li { padding: 10px; margin: 5px 0; background-color: #f0f0f0; border-radius: 5px; cursor: pointer; }"
                        +"li:hover { background-color: #e0e0e0; }"
                    }
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
                        // Отображение всех сессий в виде списка
                        sessionsGuid.forEach { guid ->
                            li {
                                // Задаем атрибут data-guid сразу при создании элемента
                                attributes["data-guid"] = guid
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
        get("/session/{guid}") {
            val guid = call.parameters["guid"]
            if (guid != null) {
                val uniqueMetricNames: Set<String> = client.get("/server/send-unique-metrics/$guid").body()
                val allFunctions: List<Function> = client.get("/server/send-functions/$guid").body()

                call.respondHtml(HttpStatusCode.OK) {
                    head {
                        meta {
                            httpEquiv = "refresh"
                            content = "$PAGE_REFRESH_INTERVAL"
                        }

                        title { +"Monitoring" }
                        style {
                            +"""
            h1 {
                font-family: Arial, Helvetica, sans-serif;
                color: black
            }
            table {
                font-family: Arial, Helvetica, sans-serif;
                width: 100%;
                border-collapse: collapse;
            }
            th, td {
                border: 1px solid black;
                padding: 8px;
                text-align: left;
            }
            th {padding-top: 12px;
                padding-bottom: 12px;
                text-align: left;
                background-color: #04AA6D;
                color: white;
            }
            tr:nth-child(even) { /*  Делает четные строки с другим фоном */
                background-color: #f2f2f2;
            }
            tr:hover {background-color: #ddd;}
            """
                        }
                    }
                    body {
                        h1 { +"Monitoring System" }

                        table {
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
                                            +f.metrics.getOrDefault(metricName, "-")
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