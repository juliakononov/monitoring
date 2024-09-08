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
        get("/client/table") {
            val uniqueMetricNames : Set<String> = client.get("/server/send-unique-metrics").body()
            val allFunctions : List<Function>  = client.get("/server/send-functions").body()

            call.respondHtml(HttpStatusCode.OK) {
                head {
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
        }
    }
}