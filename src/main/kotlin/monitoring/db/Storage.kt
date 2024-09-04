package monitoring.db

import monitoring.entities.Function
import monitoring.entities.Metric

interface Storage {
    fun saveMetrics(metrics: List<Metric>)

    fun getUniqueMetricNames() : List<String>

    fun getFunctionsToPage(pageNumber: Int, sortedBy: String) : MutableList<Function>
}