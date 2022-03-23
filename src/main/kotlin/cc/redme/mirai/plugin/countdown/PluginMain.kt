package cc.redme.mirai.plugin.countdown

import cc.redme.mirai.plugin.countdown.command.CountdownCommand
import cc.redme.mirai.plugin.countdown.data.PluginConfig
import cc.redme.mirai.plugin.countdown.data.PluginData
import net.mamoe.mirai.console.command.CommandManager
import net.mamoe.mirai.console.plugin.jvm.JvmPluginDescription
import net.mamoe.mirai.console.plugin.jvm.KotlinPlugin
import net.mamoe.mirai.event.GlobalEventChannel
import net.mamoe.mirai.event.events.FriendMessageEvent
import net.mamoe.mirai.event.events.GroupMessageEvent
import net.mamoe.mirai.utils.info

/**
 * 使用 kotlin 版请把
 * `src/main/resources/META-INF.services/net.mamoe.mirai.console.plugin.jvm.JvmPlugin`
 * 文件内容改成 `org.example.mirai.plugin.PluginMain` 也就是当前主类全类名
 *
 * 使用 kotlin 可以把 java 源集删除不会对项目有影响
 *
 * 在 `settings.gradle.kts` 里改构建的插件名称、依赖库和插件版本
 *
 * 在该示例下的 [JvmPluginDescription] 修改插件名称，id和版本，etc
 *
 * 可以使用 `src/test/kotlin/RunMirai.kt` 在 ide 里直接调试，
 * 不用复制到 mirai-console-loader 或其他启动器中调试
 */

object PluginMain : KotlinPlugin(
    JvmPluginDescription(
        id = "cc.redme.mirai.plugin.countdown",
        name = "倒计时",
        version = "0.1.0"
    ) {
        author("YehowahLiu")
        info(
            """
            一个简单的倒计时插件
        """.trimIndent()
        )
        // author 和 info 可以删除.
    }
) {
    override fun onEnable() {
        logger.info { "Countdown Plugin loaded" }
        PluginConfig.reload()
        PluginData.reload()
        CommandManager.registerCommand(CountdownCommand)
        val eventChannel = GlobalEventChannel.parentScope(this)
        eventChannel.subscribeAlways<GroupMessageEvent> {
            // 群消息
            CountdownTasker.checkKeyword(message, group)

        }
        eventChannel.subscribeAlways<FriendMessageEvent> {
            // 好友消息
            CountdownTasker.checkKeyword(message, sender)
        }
    }
}