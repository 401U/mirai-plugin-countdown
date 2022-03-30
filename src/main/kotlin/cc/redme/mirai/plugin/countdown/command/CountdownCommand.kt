package cc.redme.mirai.plugin.countdown.command

import cc.redme.mirai.plugin.countdown.CountdownTasker
import cc.redme.mirai.plugin.countdown.PluginMain
import cc.redme.mirai.plugin.countdown.data.PluginData
import cc.redme.mirai.plugin.countdown.delegate
import cc.redme.mirai.plugin.countdown.event.AddCountdownEvent
import cc.redme.mirai.plugin.countdown.event.EditCountdownEvent
import cc.redme.mirai.plugin.countdown.utils.TimeUtils
import net.mamoe.mirai.console.command.CommandSender
import net.mamoe.mirai.console.command.CommandSenderOnMessage
import net.mamoe.mirai.console.command.CompositeCommand
import net.mamoe.mirai.console.command.descriptor.CommandArgumentParserException
import net.mamoe.mirai.console.permission.PermissionService.Companion.hasPermission
import net.mamoe.mirai.contact.Contact
import net.mamoe.mirai.event.broadcast

object CountdownCommand: CompositeCommand(
    owner = PluginMain,
    primaryName = "ddl", "倒计时", "countdown", "deadline",
    description = "倒数日指令"
) {
    @SubCommand("add", "添加")
    suspend fun CommandSenderOnMessage<*>.add(name: String, time: String, contact: Contact?=null){
        val target: Contact = when{
            contact!=null && hasPermission(PluginMain.crossContactPerm)-> contact
            contact == null -> Contact()
            else -> {
                sendMessage("权限不足")
                return
            }
        }
        val timestamp = TimeUtils.inputPatternToTimestamp(time)
        if(timestamp == null) {
            sendMessage("时间格式错误")
            return
        }
        AddCountdownEvent(target, name, timestamp, fromEvent).broadcast()
    }

    @SubCommand("del", "delete", "remove", "删除")
    suspend fun CommandSender.del(index: Int, contact: Contact?=null)=sendMessage(
        when{
            contact != null && hasPermission(PluginMain.crossContactPerm) -> CountdownTasker.delCountdown(index, contact)
            contact == null -> CountdownTasker.delCountdown(index, Contact())
            else ->  "权限不足"
        }
    )

    @SubCommand("list", "page", "列表")
    suspend fun CommandSender.list(page: Int=1, contact: Contact?=null)=sendMessage(
        when{
            contact != null && hasPermission(PluginMain.crossContactPerm) -> CountdownTasker.listCountdown(page, contact)
            contact == null -> CountdownTasker.listCountdown(page, Contact())
            else ->  "权限不足"
        }
    )

    @SubCommand("edit", "修改")
    suspend fun CommandSenderOnMessage<*>.edit(index: Int, contact: Contact?=null){
        val target: Contact = when{
            contact!=null && hasPermission(PluginMain.crossContactPerm)-> contact
            contact == null -> Contact()
            else -> {
                sendMessage("权限不足")
                return
            }
        }
        if(target.delegate !in PluginData.countdown.keys || index !in 0 until PluginData.countdown[target.delegate]!!.size) {
            sendMessage("索引超出范围")
            return
        }
        EditCountdownEvent(target, index, fromEvent).broadcast()
    }

    @SubCommand("info", "查看")
    suspend fun CommandSender.info(index: Int, contact: Contact?=null)=sendMessage(
        when{
            contact != null && hasPermission(PluginMain.crossContactPerm) -> CountdownTasker.getInfo(index, contact)
            contact == null -> CountdownTasker.getInfo(index, Contact())
            else ->  "权限不足"
        }
    )

    private fun CommandSender.Contact(): Contact = subject?:throw CommandArgumentParserException("无法从当前环境获取联系人")
}