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

const val PAGE_REFRESH_INTERVAL = 0.5
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
            val uniqueMetricNames: Set<String> = client.get("/server/send-unique-metrics").body()
            val allFunctions: List<Function> = client.get("/server/send-functions").body()

            // Параметры для постраничной навигации
            val pageSize = 10 // Количество функций на странице
            val pageNumber = call.parameters["page"]?.toIntOrNull() ?: 1 // Текущая страница
            val totalFunctions = allFunctions.size
            val totalPages = (totalFunctions + pageSize - 1) / pageSize // Общее количество страниц

            // Вычисляем границы для текущей страницы
            val startIndex = (pageNumber - 1) * pageSize
            val endIndex = minOf(startIndex + pageSize, totalFunctions)
            val functionsOnCurrentPage = allFunctions.subList(startIndex, endIndex)

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
                        
                        .nav-buttons {
                            margin-top: 20px;
                        }
                        .nav-button {
                            display: inline-block;
                            padding: 10px 15px;
                            margin: 0 2px;
                            border: none;
                            border-radius: 2px;
                            background-color: #f2f2f2;
                            color: black;
                            text-decoration: none;
                            font-weight: bold;
                            transition: background-color 0.3s;
                        }
                        .nav-button:hover {
                            background-color: #ddd; /* Темнее при наведении */
                        }
                        .nav-button.active {
                            background-color: #04AA6D; /* Цвет для активной страницы */
                        }
                        .nav-button.disabled {
                            border: 1px solid black;
                            background-color: #f2f2f2; /* Серый цвет для неактивных кнопок */
                            cursor: not-allowed;
                        }
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
                        functionsOnCurrentPage.forEach { f ->
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

                    // Кнопки навигации с номерами страниц
                    div(classes = "nav-buttons") {
                        // Кнопка перехода на первую страницу
                        if (pageNumber > 1) {
                            a(href = "/client/table?page=1", classes = "nav-button active") { +"First" }
                        } else {
                            span(classes = "nav-button active") { +"First" }
                        }

                        // Кнопки для конкретных страниц
                        for (i in 1..totalPages) {
                            if (i == pageNumber) {
                                span(classes = "nav-button active") { +i.toString() } // Активная страница
                            } else {
                                a(href = "/client/table?page=$i", classes = "nav-button") { +i.toString() }
                            }
                        }

                        // Кнопка перехода на последнюю страницу
                        if (pageNumber < totalPages) {
                            a(href = "/client/table?page=$totalPages", classes = "nav-button active") { +"Last" }
                        } else {
                            span(classes = "nav-button active") { +"Last" }
                        }
                    }
                }
            }
        }
    }
}