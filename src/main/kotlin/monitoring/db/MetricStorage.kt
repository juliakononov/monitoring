package monitoring.db

import monitoring.entities.Metric
import monitoring.entities.Function
import org.jacodb.api.storage.ers.EmptyErsSettings
import org.jacodb.api.storage.ers.Entity
import org.jacodb.api.storage.ers.ErsSettings
import org.jacodb.api.storage.ers.EntityRelationshipStorage
import org.jacodb.api.storage.ers.findOrNew
import org.jacodb.impl.storage.ers.ram.RAMEntityRelationshipStorageSPI

const val NUM_OF_ELEMENT_ON_PAGE = 20

class MetricStorage : Storage {
    private val ersSettings: ErsSettings get() = EmptyErsSettings
    private val persistenceLocation: String? get() = null

    private val storage: HashMap<String, EntityRelationshipStorage> = hashMapOf()

    private fun createOrUpdateEntity(metric: Metric) {

        fun getValue(type: String, value: String): Any {
            return when (type.lowercase()) {
                "double" -> value.toDoubleOrNull() ?: throw IllegalArgumentException("Invalid value for Double")
                "int" -> value.toIntOrNull() ?: throw IllegalArgumentException("Invalid value for Int")
                "bool", "boolean" -> value.toBooleanStrictOrNull() ?: throw IllegalArgumentException("Invalid value for Boolean")
                "string" -> value
                else -> throw IllegalArgumentException("Unknown type: $type")
            }
        }

        fun saveValue(entity: Entity) {
            val value = getValue(metric.params.type, metric.params.value)

            if (metric.params.transitive) {
                entity["Transitive${metric.name}.${metric.params.type}"] = value
            } else {
                entity["${metric.name}.${metric.params.type}"] = value
            }
        }

        storage.putIfAbsent(metric.guid, RAMEntityRelationshipStorageSPI().newStorage(persistenceLocation, ersSettings))
        val session = storage.get(metric.guid)!!

        session.transactional { txn ->
            val entity = txn.findOrNew(type = "Function", property = "functionName", value = metric.params.funName)
            saveValue(entity)
        }
    }

    override fun saveMetrics(metrics: List<Metric>) {
        metrics.forEach { metric ->
            createOrUpdateEntity(metric)
        }
    }

    fun getSessionsGuid(): MutableSet<String> {
        return storage.keys
    }

    override fun getCurrentSessionMetricNames(sessionGuid: String): List<String> {
        val session = storage.getOrDefault(sessionGuid, RAMEntityRelationshipStorageSPI().newStorage(persistenceLocation, ersSettings))
        return session.transactional { txn ->
            txn.getPropertyNames("Function").map{metricName ->
                metricName.split(".")[0]
            }
        } - "functionName"
    }

    private fun getValueByType(typeName: String, e: Entity, metricName: String) : Any? {
        return when (typeName.lowercase()) {
            "double" -> e.get<Double>(metricName)
            "int" -> e.get<Int>(metricName)
            "bool", "boolean" -> e.get<Boolean>(metricName)
            "string" -> e.get<String>(metricName)
            else -> throw IllegalArgumentException("Unknown type")
        }
    }

    //TODO: добавить сортировку
    override fun getFunctionsToPage(sessionGuid: String, pageNumber: Int, sortedBy: String): MutableList<Function> {
        val functions: MutableList<Function> = mutableListOf()
        val session = storage.getOrDefault(sessionGuid, RAMEntityRelationshipStorageSPI().newStorage(persistenceLocation, ersSettings))

        session.transactional { txn ->
            val uniqueMetrics =  txn.getPropertyNames("Function") - "functionName"

            val entities = txn.all("Function")
            val entitiesToPage = entities.drop(pageNumber * NUM_OF_ELEMENT_ON_PAGE).take(NUM_OF_ELEMENT_ON_PAGE)

            entitiesToPage.forEach { e ->
                val funName: String = e["functionName"] ?: throw IllegalStateException()
                val metrics: MutableMap<String, String> = mutableMapOf()
                uniqueMetrics.forEach { metric ->
                    val nameWithType = metric.split(".")

                    val name = nameWithType[0]
                    val type = nameWithType[1]
                    val value = getValueByType(type, e, metric)

                    if (value != null) {
                        metrics[name] = value.toString()
                    }
                }
                functions.add(Function(funName, metrics))
            }
        }
        return functions
    }
}