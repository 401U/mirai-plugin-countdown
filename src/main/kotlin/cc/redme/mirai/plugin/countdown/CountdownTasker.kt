package cc.redme.mirai.plugin.countdown

import cc.redme.mirai.plugin.countdown.data.CountdownData
import cc.redme.mirai.plugin.countdown.data.PluginConfig
import cc.redme.mirai.plugin.countdown.data.PluginData
import cc.redme.mirai.plugin.countdown.utils.TimeUtils
import kotlinx.coroutines.*
import kotlinx.coroutines.sync.Mutex
import kotlinx.coroutines.sync.withLock
import net.mamoe.mirai.Bot
import net.mamoe.mirai.console.permission.AbstractPermitteeId
import net.mamoe.mirai.console.permission.PermissionService.Companion.hasPermission
import net.mamoe.mirai.console.permission.PermitteeId
import net.mamoe.mirai.contact.Contact
import net.mamoe.mirai.contact.Group
import net.mamoe.mirai.message.data.MessageChain
import net.mamoe.mirai.message.data.MessageSource.Key.quote
import net.mamoe.mirai.message.data.content
import net.mamoe.mirai.message.data.messageChainOf
import net.mamoe.mirai.message.data.time
import java.lang.Integer.min
import java.util.stream.Collectors
import kotlin.coroutines.CoroutineContext
import kotlin.time.ExperimentalTime

/**
 * 通过正负号区分群和用户
 * @author cssxsh
 */
val Contact.delegate get() = (if (this is Group) id * -1 else id).toString()


object CountdownTasker: CoroutineScope {
    override val coroutineContext: CoroutineContext = Dispatchers.IO + CoroutineName("CountdownTasker")
    private val config: PluginConfig = PluginConfig
    private val mutex = Mutex()
    private val data: MutableMap<String, MutableList<CountdownData>> = PluginData.countdown
    private var daemon: Job? = null
    private val logger = PluginMain.logger

    fun start(){
        daemon = daemonJob()
    }

    fun stop(){
        daemon?.cancel()
    }

    private fun checkNotifyPerm(idString: String): Boolean{
        val id = idString.toLong()
        val permitteeId: PermitteeId? = when{
            id>0 -> AbstractPermitteeId.ExactUser(id)
            id < 0 -> AbstractPermitteeId.ExactGroup(-1 * id)
            else -> null
        }
        return permitteeId?.hasPermission(PluginMain.notifyPerm) ?: false
    }

    private fun getContact(idString: String): Contact? {
        val id = idString.toLong()
        return when{
            id > 0 -> Bot.instances[0].getFriend(id)
            id < 0 -> Bot.instances[0].getGroup(-1 * id)
            else -> null
        }
    }

    @OptIn(ExperimentalTime::class)
    private fun daemonJob() = launch {
        while (isActive){
            runCatching {
                val curr = TimeUtils.currentTimeStamp()
                val activeContact = data.keys.stream().filter { data[it]?.isNotEmpty() ?:false }
                    .filter { checkNotifyPerm(it) }.collect(Collectors.toList())
                for(contact in activeContact){
                    val activeCountdown =
                        data[contact]?.stream()?.filter { var1 -> var1.notify && var1.timestamp > curr }
                    for (countdown in activeCountdown!!){
                        val dur = countdown.timestamp - curr
                        intervals@ for(interval in countdown.notifyInterval.asSequence().sortedDescending().toList()){
                            if(interval < dur){
                                if(dur % interval < config.daemonInterval){
                                    getContact(contact)?.sendMessage(TimeUtils.parseCountdownPattern(countdown, curr))
                                }
                               break@intervals
                            }
                        }

                    }
                }

            }.onSuccess {
                logger.info("倒计时推送成功")
                delay(1000L * config.daemonInterval)
            }.onFailure {
                logger.error("倒计时推送失败, 即将重试")
                delay((10000L..30000L).random())
            }
        }
    }

    private fun checkOrInitGroupData(contact: Contact){
        if(!data.containsKey(contact.delegate)){
            data[contact.delegate]= mutableListOf()
        }
    }

    suspend fun addCountdown(name: String, timestamp: Long, pattern: String, target: Contact, notify:Boolean, notifyInterval: List<Long>): String = mutex.withLock {
        checkOrInitGroupData(target)
        data[target.delegate]?.add(CountdownData(name, timestamp, pattern, notify, notifyInterval))
        "设置成功@${TimeUtils.currentTimeStamp()}"
    }

    suspend fun delCountdown(index: Int, contact: Contact) = mutex.withLock{
        checkOrInitGroupData(contact)
        if(index in 0 until data[contact.delegate]?.size!!){
            data[contact.delegate]?.removeAt(index)
            "删除成功"
        }else{
            "下标不合法"
        }
    }

    suspend fun listCountdown(page: Int, contact: Contact) = mutex.withLock {
        checkOrInitGroupData(contact)
        val countdownNum = data[contact.delegate]?.size ?: 0
        val pageTotal = 1 + (countdownNum / config.page_len)
        val left = (page - 1) * config.page_len
        val right = page * config.page_len
        when{
            countdownNum == 0 -> "这里空荡荡的"
            page in 1..pageTotal -> {
                var payload = ""
                for(i in left .. min(right, countdownNum - 1)){
                    payload += "[$i] ${data[contact.delegate]?.get(i)?.name}@${TimeUtils.timeStampToDate(data[contact.delegate]?.get(i)?.timestamp!!)}\n"
                }
                payload += "---第${page}页/共${pageTotal}页---"
                payload
            }
            else -> "页码无效！可用页码范围为1到$pageTotal"
        }
    }

    suspend fun checkKeyword(message: MessageChain, contact: Contact) = mutex.withLock{
        checkOrInitGroupData(contact)
        if(message.content.length < 2){
            return@withLock
        }
        var reply = messageChainOf(message.quote())
        val matchMap = mutableMapOf<Int, String>()
        for(patternString in config.query_pattern){
            val pattern = Regex(patternString)
            if(pattern.containsMatchIn(message.content)){
                var keyword = pattern.find(message.content)!!.value
                for(c in "+*.{}()|"){
                    keyword = keyword.replace(c.toString(), "\\$c")
                }
                for(i in 0 until data[contact.delegate]!!.size){
                    if(data[contact.delegate]!![i].name.contains(Regex(keyword))){
                        matchMap[i]= TimeUtils.parseCountdownPattern(data[contact.delegate]?.get(i)!!, message.time.toLong())
                    }
                }
                break
            }
        }
        if(matchMap.isNotEmpty()){
            contact.sendMessage(
                when(matchMap.size){
                    1 -> {
                        matchMap.forEach { reply = reply.plus(it.value) }
                        reply
                    }
                    else -> {
                        reply = reply.plus("找到了${matchMap.size}个倒计时")
                        matchMap.forEach{
                            reply = reply.plus("\n[${it.key}] ${it.value}")
                        }
                        reply
                    }
                }
            )
        }
        return@withLock
    }
}