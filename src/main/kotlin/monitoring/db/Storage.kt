package monitoring.db

import monitoring.entities.Function
import monitoring.entities.Metric

interface Storage {
    fun saveMetrics(metrics: List<Metric>)

    fun getCurrentSessionMetricNames(sessionGuid: String) : List<String>

    fun getFunctionsToPage(sessionGuid: String, pageNumber: Int, sortedBy: String) : MutableList<Function>
}