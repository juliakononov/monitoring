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
        get("/client/table") {
            // Получаем параметры запроса (номер страницы), по умолчанию - 1 страница
            val currentPage = call.parameters["page"]?.toIntOrNull() ?: 1
            val itemsPerPage = 10 // Количество элементов на странице

            // Получаем данные с сервера
            val uniqueMetricNames: Set<String> = client.get("/server/send-unique-metrics").body()
            val allFunctions: List<Function> = client.get("/server/send-functions").body()

            // Определяем начальный и конечный индекс для текущей страницы
            val totalItems = allFunctions.size
            val totalPages = (totalItems + itemsPerPage - 1) / itemsPerPage // Рассчитываем количество страниц
            val startIndex = (currentPage - 1) * itemsPerPage
            val endIndex = minOf(startIndex + itemsPerPage, totalItems)

            // Получаем функции для текущей страницы
            val paginatedFunctions = allFunctions.subList(startIndex, endIndex)

            call.respondHtml(HttpStatusCode.OK) {
                head {
                    meta {
                        httpEquiv = "refresh"
                        content = "$PAGE_REFRESH_INTERVAL" // Интервал автообновления
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
                            
                        .pagination {
                            display: flex;
                            justify-content: center;
                            margin-top: 20px;
                        }
                        .pagination button {
                            background-color: #f2f2f2;
                            color: black;
                            border: 1px solid black;
                            padding: 10px 20px;
                            margin: 2px;
                            border: none;
                            border-radius: 2px;
                            cursor: pointer;
                            font-size: 16px;
                        }
                        .pagination button:hover {
                            background-color: #ddd;
                        }
                        .pagination button[disabled] {
                            background-color: #737070;
                            cursor: not-allowed;
                        }
                        .pagination .active {
                            background-color: #04AA6D;
                            font-weight: bold;
                        }
                        .pagination .dots {
                            padding: 10px 20px;
                            margin: 5px;
                            font-size: 16px;
                        }
                        """.trimIndent()
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
                        paginatedFunctions.forEach { f ->
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

                    // Добавляем кнопки навигации
                    div("pagination") {
                        fun renderPageButton(page: Int) {
                            button(classes = if (page == currentPage) "active" else "") {
                                onClick = "window.location.href='/client/table?page=$page'"
                                +page.toString()
                            }
                        }

                        // Рендерим первые 2 страницы
                        for (page in 1..2) {
                            renderPageButton(page)
                        }

                        // Если текущая страница больше 4, рендерим троеточие
                        if (currentPage > 5) {
                            span("dots") { +"..." }
                        }

                        // Страницы вокруг текущей: 2 до, текущая и 2 после
                        val startRange = maxOf(3, currentPage - 2)
                        val endRange = minOf(totalPages - 2, currentPage + 2)

                        for (page in startRange..endRange) {
                            renderPageButton(page)
                        }

                        // Если текущая страница меньше, чем totalPages - 4, рендерим троеточие
                        if (currentPage < totalPages - 4) {
                            span("dots") { +"..." }
                        }

                        // Рендерим последние 2 страницы
                        for (page in (totalPages - 1)..totalPages) {
                            renderPageButton(page)
                        }
                    }
                }
            }
        }
    }
}