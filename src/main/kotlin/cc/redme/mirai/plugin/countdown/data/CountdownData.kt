package cc.redme.mirai.plugin.countdown.data

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
data class CountdownData (
    @SerialName("name")
    val name: String,

    @SerialName("timestamp")
    val timestamp: Long,

    @SerialName("pattern")
    val pattern: String = "距离{name}还有{time}",

    @SerialName("notify")
    val notify: Boolean = false,

    @SerialName("notifyInterval") // 单位秒
    val notifyInterval: List<Long> = listOf()
)

