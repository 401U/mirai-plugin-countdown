package cc.redme.mirai.plugin.countdown.event

import net.mamoe.mirai.contact.Contact
import net.mamoe.mirai.event.AbstractEvent
import net.mamoe.mirai.event.events.MessageEvent

class EditCountdownEvent(
    val target: Contact,
    val index: Int,
    val message: MessageEvent
): AbstractEvent()
