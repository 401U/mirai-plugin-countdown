package cc.redme.mirai.plugin.countdown.data

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
data class CountdownData (
    @SerialName("name")
    val name: String ="倒计时1",
    @SerialName("timestamp")
    val timestamp: Long,
    @SerialName("pattern")
    val pattern: String = "距离{name}还有{time}"
)

