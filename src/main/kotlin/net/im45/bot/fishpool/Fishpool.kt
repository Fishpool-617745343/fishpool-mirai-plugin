package net.im45.bot.fishpool

import com.google.auto.service.AutoService
import net.mamoe.mirai.console.command.CommandManager.INSTANCE.register
import net.mamoe.mirai.console.command.CommandManager.INSTANCE.unregisterAllCommands
import net.mamoe.mirai.console.command.SimpleCommand
import net.mamoe.mirai.console.command.UserCommandSender
import net.mamoe.mirai.console.plugin.jvm.JvmPlugin
import net.mamoe.mirai.console.plugin.jvm.JvmPluginDescription
import net.mamoe.mirai.console.plugin.jvm.KotlinPlugin
import java.net.InetSocketAddress
import java.net.Socket
import java.net.URL
import kotlin.math.abs

@AutoService(JvmPlugin::class)
object Fishpool : KotlinPlugin(
        JvmPluginDescription(
                "net.im45.bot.fishpool",
                "0.2.1"
        )
) {
    override fun onEnable() {
        super.onEnable()

        OhCmd.register()
        NaCmd.register()
        PaCmd.register()
        Errcode.register()
    }

    override fun onDisable() {
        super.onDisable()

        unregisterAllCommands()
    }
}

object OhCmd : SimpleCommand(
        Fishpool, "oh"
) {
    @Handler
    suspend fun UserCommandSender.oh(h: Short) {
        val hs = "H".repeat(abs(h.toInt()))
        sendMessage(if (h > 0) "O${hs}" else "${hs}O")
    }
}

object NaCmd : SimpleCommand(
        Fishpool, "na"
) {
    @Handler
    suspend fun UserCommandSender.na(n: Short) {
        val ns = "呐".repeat(abs(n.toInt()))
        if (ns.isNotEmpty()) sendMessage(ns)
    }
}

object PaCmd : SimpleCommand(
        Fishpool, "pa"
) {
    @Handler
    suspend fun UserCommandSender.pa(p: Short) {
        val ps = "爬".repeat(abs(p.toInt()))
        if (ps.isNotEmpty()) sendMessage(ps)
    }
}

object Errcode : SimpleCommand(
        Fishpool, "errcode",
        description = "err.code"
) {
    private class LJYYSException(host: String) : Exception(host)

    private val ERRCODE = URL("https://dev.zapic.cc/err.code")

    @Handler
    suspend fun UserCommandSender.errcode() {
        ERRCODE.readText().let { ec ->
            Socket().runCatching {
                connect(InetSocketAddress(ec, 25565), 500)
                close()
            }.let { res ->
                val out = LJYYSException(if (res.isFailure) "Closed" else ec).run {
                    val lim = 3
                    val sb = StringBuilder("Exception in thread \"${Thread.currentThread().name}\": $this")
                    stackTrace.take(lim).forEach { sb.append("\n\tat $it") }
                    if (stackTrace.size > lim) sb.append("\n\t... ${stackTrace.size - lim} more")

                    sb.toString()
                }

                sendMessage(out)
            }
        }
    }
}
