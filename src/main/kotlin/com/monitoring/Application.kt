package com.monitoring

import io.ktor.http.*
import io.ktor.server.application.*
import io.ktor.server.engine.*
import io.ktor.server.netty.*
import io.ktor.serialization.kotlinx.json.*
import io.ktor.server.html.*
import kotlinx.serialization.Serializable
import io.ktor.server.routing.*
import io.ktor.server.plugins.contentnegotiation.*
import io.ktor.server.request.*
import io.ktor.server.response.*
import kotlinx.html.*
import java.util.concurrent.ConcurrentHashMap

@Serializable
data class Metric(
    val name: String,
    val params: Params
)

@Serializable
data class Params(
    val funName: String,
    val value: Double,
    val transitive: Boolean
)


class MetricStorage {
    // Хэшмапа для хранения метрик по funName
    private val storage = ConcurrentHashMap<String, MutableList<Metric>>()

    fun saveMetrics(metrics: List<Metric>) {
        metrics.forEach { metric ->
            val funName = metric.params.funName
            storage.getOrPut(funName) { mutableListOf() }.add(metric)
        }
    }

    fun getAllMetrics(): Map<String, List<Metric>> = storage

    fun getUniqueMetricNames(): Set<String> = storage.values.flatten().map { it.name }.toSet()
}

val metricStorage = MetricStorage()

fun Route.metricRoutes() {
    post("/process-metrics") {
        // Получаем массив Metric объектов из запроса и десериализуем их
        val metrics = call.receive<List<Metric>>()

        // Сохраняем метрики
        metricStorage.saveMetrics(metrics)

        // Отправляем ответ
        call.respond(HttpStatusCode.Created, "Successfully processed and saved ${metrics.size} metrics")
    }

    get("/metrics/table") {
        val allMetrics = metricStorage.getAllMetrics()
        val uniqueMetricNames = metricStorage.getUniqueMetricNames().sorted()

        call.respondHtml(HttpStatusCode.OK) {
            head {
                title { +"Metrics Table" }
                style {
                    +"""
                        table {
                                    width: 100%;
                                    border-collapse: collapse;
                                }
                                th, td {
                                    border: 1px solid black;
                                    padding: 8px;
                                    text-align: left;
                                }
                                th {
                                    background-color: #f2f2f2;
                                }
                        """
                }
            }
            body {
                h1 { +"Metrics Table" }
                table {
                    tr {
                        th { +"Function Name" }
                        uniqueMetricNames.forEach { metricName ->
                            th { +metricName }
                        }
                    }
                    allMetrics.forEach { (funName, metrics) ->
                        tr {
                            td { +funName }
                            uniqueMetricNames.forEach { metricName ->
                                td {
                                    val metric = metrics.find { it.name == metricName }
                                    if (metric != null) {
                                        +"${metric.params.value} ${metric.params.transitive}"
                                    } else {
                                        +"-"
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

fun main() {
    embeddedServer(Netty, port = 8080){
        install(ContentNegotiation) {
            json() // Устанавливаем JSON-сериализацию
        }

        routing {
            metricRoutes() // Добавляем маршруты
        }
    }.start(wait = true)
}

