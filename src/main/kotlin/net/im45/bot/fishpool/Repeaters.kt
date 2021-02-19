package net.im45.bot.fishpool

import net.mamoe.mirai.console.command.SimpleCommand
import net.mamoe.mirai.console.command.UserCommandSender
import kotlin.math.abs

object OhCmd : SimpleCommand(
    Fishpool, "oh",
    description = "OHHHHHHHH"
) {
    @Handler
    suspend fun UserCommandSender.oh(h: Short = 16) {
        val hs = "H".repeat(abs(h.toInt()))
        sendMessage(if (h > 0) "O${hs}" else "${hs}O")
    }
}

object NaCmd : SimpleCommand(
    Fishpool, "na",
    description = "呐"
) {
    @Handler
    suspend fun UserCommandSender.na(n: Short = 2) {
        val ns = "呐".repeat(abs(n.toInt()))
        if (ns.isNotEmpty()) sendMessage(ns)
    }
}

object PaCmd : SimpleCommand(
    Fishpool, "pa",
    description = "爬"
) {
    @Handler
    suspend fun UserCommandSender.pa(p: Short = 1) {
        val ps = "爬".repeat(abs(p.toInt()))
        if (ps.isNotEmpty()) sendMessage(ps)
    }
}
