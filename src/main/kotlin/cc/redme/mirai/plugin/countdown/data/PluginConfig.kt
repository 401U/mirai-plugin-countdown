package cc.redme.mirai.plugin.countdown.data

import net.mamoe.mirai.console.data.AutoSavePluginConfig
import net.mamoe.mirai.console.data.ValueDescription
import net.mamoe.mirai.console.data.value

object PluginConfig: AutoSavePluginConfig("Countdown") {
    @ValueDescription("计时提示的时间,")
    val notify: Set<String> by value(setOf(""))

    @ValueDescription("插件管理员")
    val admin: Set<String> by value(setOf(""))

    @ValueDescription("列表每页行数")
    val page_len by value(10)

    @ValueDescription("提问句型")
    val query_pattern by value(listOf("(?<=距)\\S+(?=还有)", "(?<=什么时候)\\S+", "\\S+"))
}