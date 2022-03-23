package cc.redme.mirai.plugin.countdown

import cc.redme.mirai.plugin.countdown.command.CountdownCommand
import cc.redme.mirai.plugin.countdown.data.PluginConfig
import cc.redme.mirai.plugin.countdown.data.PluginData
import net.mamoe.mirai.console.command.CommandManager
import net.mamoe.mirai.console.permission.PermissionService
import net.mamoe.mirai.console.plugin.jvm.JvmPluginDescription
import net.mamoe.mirai.console.plugin.jvm.KotlinPlugin
import net.mamoe.mirai.event.GlobalEventChannel
import net.mamoe.mirai.event.events.FriendMessageEvent
import net.mamoe.mirai.event.events.GroupMessageEvent
import net.mamoe.mirai.utils.info

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
    }
) {
    val notifyPerm by lazy { PermissionService.INSTANCE.register(permissionId("notify"), "主动推送倒计时提醒的权限", parentPermission) }
    val crossContactPerm by lazy {
        PermissionService.INSTANCE.register(permissionId("cross-contact"), "跨联系人控制插件的权限", parentPermission)
    }
    override fun onEnable() {
        logger.info { "Countdown Plugin loaded" }
        PluginConfig.reload()
        PluginData.reload()
        CommandManager.registerCommand(CountdownCommand)
        notifyPerm
        crossContactPerm
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