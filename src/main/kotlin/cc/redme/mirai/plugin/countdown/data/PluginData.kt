package cc.redme.mirai.plugin.countdown.data

import net.mamoe.mirai.console.data.AutoSavePluginData
import net.mamoe.mirai.console.data.value

object PluginData: AutoSavePluginData("data") {
    val countdown: MutableMap<Long, MutableList<CountdownData>> by value()
}