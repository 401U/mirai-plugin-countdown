package cc.redme.mirai.plugin.countdown.utils

import cc.redme.mirai.plugin.countdown.data.CountdownData
import java.text.SimpleDateFormat
import java.time.LocalDateTime
import java.time.ZoneOffset
import java.time.format.DateTimeFormatter
import java.time.format.DateTimeParseException
import java.time.temporal.ChronoUnit
import kotlin.time.Duration.Companion.seconds
import kotlin.time.ExperimentalTime

object TimeUtils {
    // regex patterns
    private val patterns = listOf(
        Regex("^\\d{4,}(-\\d{1,2}){2}(\\.\\d{1,2}(:\\d{1,2}){0,2})?\$"),
        Regex("^\\d{1,2}(:\\d{1,2}){1,2}\$"),
        Regex("^(\\d+年)?(\\d+个月)?(\\d+天)?(\\d+小时)?(\\d+分钟)?(\\d+秒)?(后|之后|以后)\$")
    )
    private val formatters = listOf<DateTimeFormatter>(
        DateTimeFormatter.ofPattern("y-M-d.H:m:s"),
        DateTimeFormatter.ofPattern("y-M-d.H:m"),
        DateTimeFormatter.ofPattern("y-M-d.H"),
        DateTimeFormatter.ofPattern("y-M-d"),
        DateTimeFormatter.ofPattern("H:m:s"),
        DateTimeFormatter.ofPattern("H:m")
    )

    /* 将用户输入的时间格式转换为时间戳形式 */
    fun inputPatternToTimestamp(timeString: String): Long?{
        var payload: Long
        when{
            timeString.matches(patterns[0]) || timeString.matches(patterns[1]) -> {
                for (formatter in formatters) {
                    try {
                        payload = LocalDateTime.parse(timeString, formatter).toEpochSecond(ZoneOffset.of("+8"))
                        return payload
                    } catch (e: DateTimeParseException) {
                        continue
                    }
                }
                return null
            }
            timeString.matches(patterns[2]) -> {
                val current = LocalDateTime.now()
                return current.plus(Regex("\\d+(?=年)").find(timeString)?.value?.toLong()?:0L, ChronoUnit.YEARS)
                    .plus(Regex("\\d+(?=个月)").find(timeString)?.value?.toLong()?:0L, ChronoUnit.MONTHS)
                    .plus(Regex("\\d+(?=天)").find(timeString)?.value?.toLong()?:0L, ChronoUnit.DAYS)
                    .plus(Regex("\\d+(?=小时)").find(timeString)?.value?.toLong()?:0L, ChronoUnit.HOURS)
                    .plus(Regex("\\d+(?=分钟)").find(timeString)?.value?.toLong()?:0L, ChronoUnit.MINUTES)
                    .plus(Regex("\\d+(?=秒)").find(timeString)?.value?.toLong()?:0L, ChronoUnit.SECONDS)
                    .toEpochSecond(ZoneOffset.of("+8"))
            }
            else -> return null
        }
    }

    /* 获取当前时间戳 */
    fun currentTimeStamp():Long=LocalDateTime.now().toEpochSecond(ZoneOffset.of("+8"))

    /* 时间戳转换为友好的时间格式 */
    fun timeStampToDate(timestamp: Long): String=SimpleDateFormat("y-M-d H:m:s").format(timestamp*1000)

    /* 计算倒计时距离,并转化为更友好的的时间格式  */
    @OptIn(ExperimentalTime::class)
    fun parseCountdownPattern(date: CountdownData, curr: Long): String {
        var payload = ""
        val duration =
            if(curr > date.timestamp){
                (curr - date.timestamp).seconds
            }else{
                (date.timestamp - curr).seconds
            }
        if(duration.inWholeDays > 0){
            payload += "${duration.inWholeDays}天"
        }
        if(duration.inWholeHours % 24 > 0){
            payload += "${duration.inWholeHours % 24}个小时"
        }
        if(duration.inWholeMinutes % 60 > 0){
            payload += "${duration.inWholeMinutes % 60}分钟"
        }
        if(duration.inWholeSeconds % 60 > 0){
            payload += "${duration.inWholeSeconds % 60}秒"
        }
        return date.pattern.replace("{name}", date.name).replace("{time}", payload)
    }
}