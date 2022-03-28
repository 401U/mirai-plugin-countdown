package cc.redme.mirai.plugin.countdown.data

import cc.redme.mirai.plugin.countdown.utils.TimeUtils
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
){
    override fun toString(): String {
        return """
            [0] 名称: $name
            [1] 时间: ${TimeUtils.timeStampToDate(timestamp)}
            [2] 通知格式: $pattern
            [3] 通知开关: $notify
            [4] 通知周期: $notifyInterval
        """.trimIndent()
    }
}
