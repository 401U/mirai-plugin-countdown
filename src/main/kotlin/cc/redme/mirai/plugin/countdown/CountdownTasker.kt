package cc.redme.mirai.plugin.countdown

import cc.redme.mirai.plugin.countdown.data.CountdownData
import cc.redme.mirai.plugin.countdown.data.PluginConfig
import cc.redme.mirai.plugin.countdown.data.PluginData
import cc.redme.mirai.plugin.countdown.utils.TimeUtils
import kotlinx.coroutines.CoroutineName
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.sync.Mutex
import kotlinx.coroutines.sync.withLock
import net.mamoe.mirai.contact.Contact
import net.mamoe.mirai.message.data.MessageChain
import net.mamoe.mirai.message.data.MessageSource.Key.quote
import net.mamoe.mirai.message.data.content
import net.mamoe.mirai.message.data.messageChainOf
import net.mamoe.mirai.message.data.time
import java.lang.Integer.min
import kotlin.coroutines.CoroutineContext

object CountdownTasker: CoroutineScope {
    override val coroutineContext: CoroutineContext = Dispatchers.IO + CoroutineName("CountdownTasker")
    private val config: PluginConfig = PluginConfig
    private val mutex = Mutex()
    private val data: MutableMap<Long, MutableList<CountdownData>> = PluginData.countdown

    fun start(){

    }

    fun stop(){}

    fun parseTimeStamp(time: String): Long? {
        // 2022-03-21.10:29:25
        // 2022-03-21
        // 10:29(:25)
        // ..年..个月..天..小时..分钟..秒后

        val pattern1 = Regex("^\\d{4,}(-\\d{1,2}){2}(\\.\\d{1,2}(:\\d{1,2}){0,2})?\$")
        //val pattern2 = Regex("^\\d{1,2}(:\\d{1,2}){1,2}\$")
        val pattern3 = Regex("^(\\d+年)?(\\d+个月)?(\\d+天)?(\\d+小时)?(\\d+分钟)?(\\d+秒)?(后|之后|以后)\$")
        return when{
            time.matches(pattern1) -> {
                1
            }
            // time.matches(pattern2) -> 2
            time.matches(pattern3) -> {
                2
            }
            else -> null
        }
    }

    private fun checkOrInitGroupData(contact: Long){
        if(!data.containsKey(contact)){
            data[contact]= mutableListOf()
        }
    }

    suspend fun addCountdown(name: String, timeString: String, pattern: String, contact: Long): String = mutex.withLock {
        checkOrInitGroupData(contact)
        val timestamp = TimeUtils.patternToTimestamp(timeString)
        if (timestamp == null) {
            "不合法的时间格式!"
        } else {
            checkOrInitGroupData(contact)
            data[contact]?.add(CountdownData(name, timestamp, pattern))
            "设置成功@${TimeUtils.currentTimeStamp()}"
        }
    }

    suspend fun delCountdown(index: Int, contact: Long) = mutex.withLock{
        checkOrInitGroupData(contact)
        if(index in 0 until data[contact]?.size!!){
            data[contact]?.removeAt(index)
            "删除成功"
        }else{
            "下标不合法"
        }
    }

    suspend fun listCountdown(page: Int, contact: Long) = mutex.withLock {
        checkOrInitGroupData(contact)
        val countdownNum = data[contact]?.size ?: 0
        val pageTotal = 1 + (countdownNum / config.page_len)
        val left = (page - 1) * config.page_len
        val right = page * config.page_len
        when{
            countdownNum == 0 -> "这里空荡荡的"
            page in 1..pageTotal -> {
                var payload = ""
                for(i in left .. min(right, countdownNum - 1)){
                    payload += "[$i] ${data[contact]?.get(i)?.name}@${TimeUtils.timeStampToDate(data[contact]?.get(i)?.timestamp!!)}\n"
                }
                payload += "---第${page}页/共${pageTotal}页---"
                payload
            }
            else -> "页码无效！可用页码范围为1到$pageTotal"
        }
    }

    suspend fun enableNotify(contact: Long) = mutex.withLock {
        checkOrInitGroupData(contact)
    }

    suspend fun checkKeyword(message: MessageChain, contact: Contact) = mutex.withLock{
        checkOrInitGroupData(contact.id)
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
                for(i in 0 until data[contact.id]!!.size){
                    if(data[contact.id]!![i].name.contains(Regex(keyword))){
                        matchMap[i]= TimeUtils.parseCountdownPattern(data[contact.id]?.get(i)!!, message.time.toLong())
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