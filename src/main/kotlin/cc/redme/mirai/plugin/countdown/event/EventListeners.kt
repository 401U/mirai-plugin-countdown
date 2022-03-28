package cc.redme.mirai.plugin.countdown.event

import cc.redme.mirai.plugin.countdown.CountdownTasker
import cc.redme.mirai.plugin.countdown.data.PluginConfig
import cc.redme.mirai.plugin.countdown.utils.TimeUtils
import net.mamoe.mirai.event.EventHandler
import net.mamoe.mirai.event.SimpleListenerHost
import net.mamoe.mirai.event.events.MessageEvent
import net.mamoe.mirai.event.selectMessages

object EventListeners: SimpleListenerHost(){
    @EventHandler
    suspend fun AddCountdownEvent.onEvent(){
        var notify = false
        var pattern = "距离{name}还有{time}"
        var notifyInterval: List<Long> = TimeUtils.parseIntervalList(PluginConfig.defaultNotifyInterval)
        message.subject.sendMessage("是否开启倒计时提醒? 0: 是 1: 否\n请回复 0 或 1 ")
        message.selectMessages<MessageEvent, Unit> {
            "0"{
                notify = true
            }
            "1"{
                notify = false
            }
            defaultQuoteReply { "格式错误, 使用默认值(不提醒)" }

            timeout(90_000){
                message.subject.sendMessage("超时, 使用默认值(不提醒)")
            }
        }
        if(notify){
            message.subject.sendMessage("请设置提醒的周期, 格式如：\n1天,1d,1小时,1h,1分钟,1min,1m,1秒,1s\n多个提醒周期用英文逗号分隔开,回复 0 使用默认值")
            message.selectMessages<MessageEvent, Unit> {
                "0" { }
                default {
                    val temp =  TimeUtils.parseIntervalList(this.message.toString())
                    if(temp.isEmpty()){
                        subject.sendMessage("未找到合法的提醒周期, 将使用默认值")
                    }else{
                        notifyInterval = temp
                    }
                }
                timeout(90_000){
                    message.subject.sendMessage("超时, 使用默认值(${PluginConfig.defaultNotifyInterval})")
                }
            }
        }

        message.subject.sendMessage("请设置倒计时的回复格式, 回复 0 来使用默认值(距离{name}还有{time})")
        message.selectMessages<MessageEvent, Unit> {
            "0"{
                pattern = "距离{name}还有{time}"
            }
            default {
                pattern = this.message.toString()
            }
            timeout(90_000){
                message.subject.sendMessage("超时, 使用默认值(距离{name}还有{time})")
            }
        }
        message.subject.sendMessage(CountdownTasker.addCountdown(name, time, pattern, target, notify, notifyInterval))
    }
}