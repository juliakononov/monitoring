package monitoring.entities

import kotlinx.serialization.Serializable

@Serializable
data class Metric(
    val guid: String,
    val name: String,
    val params: Params
)

@Serializable
data class Params(
    val funName: String,
    val type: String,
    val value: String,
    val transitive: Boolean
)