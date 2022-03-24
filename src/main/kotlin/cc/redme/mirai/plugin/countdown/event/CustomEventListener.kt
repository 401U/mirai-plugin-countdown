package cc.redme.mirai.plugin.countdown.event

import cc.redme.mirai.plugin.countdown.CountdownTasker
import cc.redme.mirai.plugin.countdown.PluginMain
import cc.redme.mirai.plugin.countdown.data.PluginConfig
import cc.redme.mirai.plugin.countdown.utils.TimeUtils
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.cancelChildren
import net.mamoe.mirai.console.util.ConsoleExperimentalApi
import net.mamoe.mirai.console.util.CoroutineScopeUtils.childScope
import net.mamoe.mirai.event.GlobalEventChannel
import net.mamoe.mirai.event.events.FriendMessageEvent
import net.mamoe.mirai.event.events.GroupMessageEvent
import net.mamoe.mirai.event.globalEventChannel
import net.mamoe.mirai.event.selectMessagesUnit
import net.mamoe.mirai.utils.MiraiExperimentalApi

@OptIn(ConsoleExperimentalApi::class)
internal object CustomEventListener: CoroutineScope by PluginMain.childScope("CustomEventListener") {
    @OptIn(MiraiExperimentalApi::class)
    fun subscribe(){
        GlobalEventChannel.subscribeAlways<GroupMessageEvent> {
            // 群消息
            CountdownTasker.checkKeyword(message, group)
        }
        GlobalEventChannel.subscribeAlways<FriendMessageEvent> {
            // 好友消息
            CountdownTasker.checkKeyword(message, sender)
        }
        globalEventChannel().subscribeAlways<AddCountdownEvent> {
            var notify = false
            var pattern = "距离{name}还有{time}"
            var notifyInterval: List<Long> = TimeUtils.parseIntervalList(PluginConfig.defaultNotifyInterval)
            message.subject.sendMessage("是否开启倒计时提醒? 0: 是 1: 否\n请回复 0 或 1 ")
            message.selectMessagesUnit {
                "0"{
                    notify = true
                }
                "1"{
                    notify = false
                }
                defaultQuoteReply { "格式错误, 使用默认值(不提醒)" }

                timeout(30_000).quoteReply("超时, 使用默认值(不提醒)")
            }
            if(notify){
                message.subject.sendMessage("请设置提醒的周期, 格式如：\n1天,1d,1小时,1h,1分钟,1min,1m,1秒,1s\n多个提醒周期用英文逗号分隔开,回复 0 使用默认值")
                message.selectMessagesUnit {
                    "0" { }
                    default {
                        val temp =  TimeUtils.parseIntervalList(this.message.toString())
                        if(temp.isEmpty()){
                            subject.sendMessage("未找到合法的提醒周期, 将使用默认值")
                        }else{
                            notifyInterval = temp
                        }
                    }
                    timeout(30_000).quoteReply("超时, 使用默认值(${PluginConfig.defaultNotifyInterval})")
                }
            }

            message.subject.sendMessage("请设置倒计时的回复格式, 回复 0 来使用默认值(距离{name}还有{time})")
            message.selectMessagesUnit {
                "0"{
                    pattern = "距离{name}还有{time}"
                }
                default {
                    pattern = this.message.toString()
                }
                timeout(30_000).quoteReply("超时, 使用默认值(距离{name}还有{time})")
            }
            message.subject.sendMessage(CountdownTasker.addCountdown(name, time, pattern, target, notify, notifyInterval))
        }
    }

    fun stop(){
        coroutineContext.cancelChildren()
    }
}