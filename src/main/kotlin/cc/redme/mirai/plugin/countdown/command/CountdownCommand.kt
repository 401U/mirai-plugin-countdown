package cc.redme.mirai.plugin.countdown.command

import cc.redme.mirai.plugin.countdown.CountdownTasker
import cc.redme.mirai.plugin.countdown.PluginMain
import net.mamoe.mirai.console.command.CommandSender
import net.mamoe.mirai.console.command.CompositeCommand
import net.mamoe.mirai.console.command.descriptor.CommandArgumentParserException
import net.mamoe.mirai.contact.Contact

object CountdownCommand: CompositeCommand(
    owner = PluginMain,
    primaryName = "ddl", "倒计时", "countdown", "deadline",
    description = "倒数日指令"
) {
    @SubCommand("add", "添加")
    suspend fun CommandSender.add(name: String, time: String, pattern: String = "距离{name}还有{time}", contact: Contact=Contact())=sendMessage(
        CountdownTasker.addCountdown(name, time, pattern, contact)
    )

    @SubCommand("del", "delete", "remove", "删除")
    suspend fun CommandSender.del(index: Int, contact: Contact=Contact())=sendMessage(
        CountdownTasker.delCountdown(index, contact)
    )

    @SubCommand("list", "page", "列表")
    suspend fun CommandSender.list(page: Int=1, contact: Contact=Contact())=sendMessage(
        CountdownTasker.listCountdown(page, contact)
    )

    private fun CommandSender.Contact(): Contact = subject?:throw CommandArgumentParserException("无法从当前环境获取联系人")
}