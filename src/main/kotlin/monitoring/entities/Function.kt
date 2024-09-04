package monitoring.entities

import kotlinx.serialization.Serializable

@Serializable
data class Function(
    val funName: String,
    val metrics: MutableMap<String, String>
)