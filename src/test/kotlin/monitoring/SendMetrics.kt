package monitoring

import kotlinx.coroutines.runBlocking
import kotlinx.serialization.encodeToString
import kotlinx.serialization.json.Json
import java.net.HttpURLConnection
import java.net.URL
import kotlin.random.Random
import kotlin.concurrent.fixedRateTimer
import monitoring.entities.Metric
import monitoring.entities.Params
import java.io.OutputStreamWriter
import java.util.*

fun generateRandomMetric(guid: String): Metric {
    val metricName = "Metric${Random.nextInt(1, 10)}"
    val funName = "Function${Random.nextInt(1, 201)}"
    val type = listOf("Boolean", "Int", "String", "Double").random()
    val value = when (type) {
        "Boolean" -> Random.nextBoolean().toString()
        "Int" -> Random.nextInt(0, 100).toString()
        "String" -> "str" + Random.nextInt(0, 100).toString()
        "Double" -> Random.nextDouble(0.0, 100.0).toString()
        else -> ""
    }
    val transitive = Random.nextBoolean()

    return Metric(
        guid = guid,
        name = metricName,
        //TODO: исправить работу
        params = Params(
            funName = funName,
            type = type,
            value = value,
            transitive = transitive
        )
    )
}

fun sendMetrics(metrics: List<Metric>) {
    val url = URL("http://localhost:8080/metrics")
    val connection = url.openConnection() as HttpURLConnection
    connection.requestMethod = "POST"
    connection.setRequestProperty("Content-Type", "application/json")
    connection.doOutput = true

    val jsonMetrics = Json.encodeToString(metrics)
    val outputStreamWriter = OutputStreamWriter(connection.outputStream)
    outputStreamWriter.write(jsonMetrics)
    outputStreamWriter.flush()

    val responseCode = connection.responseCode
    println("Response Code: $responseCode")

    outputStreamWriter.close()
    connection.disconnect()
}

fun main() {
    runBlocking {
        val delta: Long = 500  //в миллисекундах
        val guid = UUID.randomUUID().toString()


        fixedRateTimer("metricSender", initialDelay = 0, period = delta) {
            val randomSize = Random.nextInt(1, 100)
            val metrics = List(randomSize) { generateRandomMetric(guid) }
            sendMetrics(metrics)
        }
    }
}