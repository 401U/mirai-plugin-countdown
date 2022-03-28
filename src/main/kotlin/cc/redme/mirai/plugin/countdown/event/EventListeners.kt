package cc.redme.mirai.plugin.countdown.event

import cc.redme.mirai.plugin.countdown.CountdownTasker
import cc.redme.mirai.plugin.countdown.data.CountdownData
import cc.redme.mirai.plugin.countdown.data.PluginConfig
import cc.redme.mirai.plugin.countdown.data.PluginData
import cc.redme.mirai.plugin.countdown.delegate
import cc.redme.mirai.plugin.countdown.utils.TimeUtils
import kotlinx.coroutines.sync.withLock
import net.mamoe.mirai.event.EventHandler
import net.mamoe.mirai.event.SimpleListenerHost
import net.mamoe.mirai.event.events.MessageEvent
import net.mamoe.mirai.event.selectMessages
import net.mamoe.mirai.message.data.content

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
            defaultReply { "格式错误, 使用默认值(不提醒)" }

            timeout(90_000){
                message.subject.sendMessage("超时, 使用默认值(不提醒)")
            }
        }
        if(notify){
            message.subject.sendMessage("请设置提醒的周期, 格式如：\n1天,1d,1小时,1h,1分钟,1min,1m,1秒,1s\n多个提醒周期用英文逗号分隔开,回复 0 使用默认值")
            message.selectMessages<MessageEvent, Unit> {
                "0" { }
                default {
                    val temp =  TimeUtils.parseIntervalList(this.message.content)
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
                pattern = this.message.content
            }
            timeout(90_000){
                message.subject.sendMessage("超时, 使用默认值(距离{name}还有{time})")
            }
        }
        message.subject.sendMessage(CountdownTasker.addCountdown(name, time, pattern, target, notify, notifyInterval))
    }

    @EventHandler
    suspend fun EditCountdownEvent.onEvent()=CountdownTasker.mutex.withLock {
        val countdownData: CountdownData = PluginData.countdown[target.delegate]?.get(index)!!.copy()
        var editTarget: Int = -1
        var interact = true
        var continueEdit = true
        var needSave = false
        var name = countdownData.name
        var interval: List<Long> = countdownData.notifyInterval
        var pattern = countdownData.pattern
        var time: Long? = countdownData.timestamp
        var notify = countdownData.notify

        message.subject.sendMessage("将要修改的倒计时信息为:\n$countdownData\n回复 i 交互修改, 回复数字修改对应的设置")


        message.selectMessages<MessageEvent, Unit> {
            "i" {}
            default {
                interact = false
                editTarget = when(this.message.content){
                    "0" -> 0
                    "1" -> 1
                    "2" -> 2
                    "3" -> 3
                    "4" -> 4
                    else -> -1
                }
                if(editTarget == -1){
                    subject.sendMessage("无效的选项, 已退出修改模式")
                    continueEdit = false
                }
            }
            timeout(90_000){
                continueEdit = false
                message.subject.sendMessage("超时, 已退出修改模式")
            }
        }

        while(continueEdit){
            when(editTarget){
                -1 -> {}
                0 -> {
                    message.subject.sendMessage("请输入倒计时名称, 回复 0 保持原设置")
                    message.selectMessages {
                        "0"{}
                        default {
                            name = this.message.content
                            needSave = true
                        }
                        timeout(90_000){
                            message.subject.sendMessage("超时, 退出修改模式")
                            continueEdit = false
                        }
                    }
                }
                1 -> {
                    message.subject.sendMessage("请输入倒计时时间, 回复 0 保持原设置")
                    message.selectMessages {
                        "0"{}
                        default {
                            time = TimeUtils.inputPatternToTimestamp(this.message.content)
                            if(time == null){
                                subject.sendMessage("输入的时间格式无效, 将保持原设置")
                                time = countdownData.timestamp
                            }else{
                                needSave = true
                            }
                        }
                        timeout(90_000){
                            message.subject.sendMessage("超时, 退出修改模式")
                            continueEdit = false
                        }
                    }
                }
                2 -> {
                    message.subject.sendMessage("请输入倒计时回复格式, 回复 0 保持原设置")
                    message.selectMessages {
                        "0"{}
                        default {
                            pattern = this.message.content
                            needSave = true
                        }
                        timeout(90_000){
                            message.subject.sendMessage("超时, 退出修改模式")
                            continueEdit = false
                        }
                    }
                }
                3 -> {
                    message.subject.sendMessage("请输入倒计时通知开关, 回复 0 保持原设置, 1 开启, 2 关闭")
                    message.selectMessages {
                        "0"{}
                        "1"{
                            notify = true
                            needSave = true
                        }
                        "2"{
                            notify = false
                            needSave = true
                        }
                        default {
                                subject.sendMessage("输入无效, 将保持原设置")
                        }
                        timeout(90_000){
                            message.subject.sendMessage("超时, 退出修改模式")
                            continueEdit = false
                        }
                    }
                }
                4 -> {
                    message.subject.sendMessage("请输入倒计时通知间隔, 回复 0 保持原设置")
                    message.selectMessages {
                        "0"{}
                        default {
                            interval = TimeUtils.parseIntervalList(this.message.content)
                            if(interval.isEmpty()){
                                subject.sendMessage("输入的时间格式无效, 将保持原设置")
                                interval = countdownData.notifyInterval
                            }else{ needSave = true }
                        }
                        timeout(90_000){
                            message.subject.sendMessage("超时, 退出修改模式")
                            continueEdit = false
                        }
                    }
                }
                else -> {
                    continueEdit = false
                }
            }
            if(continueEdit){ continueEdit = interact }
            editTarget++
        }
        if(needSave){
            PluginData.countdown[target.delegate]?.set(index, CountdownData(name, time!!, pattern, notify, interval))
            message.subject.sendMessage("修改成功")
        }
    }

    @EventHandler
    suspend fun MessageEvent.onEvent(){
        if (message.content.length >= 2 && !CountdownTasker.mutex.isLocked) CountdownTasker.checkKeyword(message, subject)
    }
}