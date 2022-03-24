package cc.redme.mirai.plugin.countdown.event

import net.mamoe.mirai.contact.Contact
import net.mamoe.mirai.event.AbstractEvent
import net.mamoe.mirai.event.events.MessageEvent

class AddCountdownEvent (
    val target: Contact,
    val name: String,
    val time: Long,
    val message: MessageEvent
): AbstractEvent()