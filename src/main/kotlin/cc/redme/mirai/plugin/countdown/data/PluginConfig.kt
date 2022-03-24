package cc.redme.mirai.plugin.countdown.data

import net.mamoe.mirai.console.data.AutoSavePluginConfig
import net.mamoe.mirai.console.data.ValueDescription
import net.mamoe.mirai.console.data.value

object PluginConfig: AutoSavePluginConfig("Countdown") {
    @ValueDescription("默认的计时提醒周期")
    val defaultNotifyInterval by value("7d,1d,8h,30m")

    @ValueDescription("守护进程检测周期, 单位为秒, 设为-1来禁用")
    val daemonInterval by value(3600)

    @ValueDescription("列表每页行数")
    val page_len by value(10)

    @ValueDescription("提问句型")
    val query_pattern by value(listOf("(?<=距)\\S+(?=还有)", "(?<=什么时候)\\S+", "\\S+"))
}