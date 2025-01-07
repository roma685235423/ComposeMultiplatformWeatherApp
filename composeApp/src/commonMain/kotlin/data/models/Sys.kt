package data.models
import kotlinx.serialization.Serializable

@Serializable
data class Sys(
    val country: String,
    val id: Int?=null,
    val sunrise: Int,
    val sunset: Int,
    val type: Int?=null
)