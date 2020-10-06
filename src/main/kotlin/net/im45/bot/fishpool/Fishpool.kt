package net.im45.bot.fishpool

import com.google.auto.service.AutoService
import net.mamoe.mirai.console.command.CommandManager.INSTANCE.register
import net.mamoe.mirai.console.command.CommandManager.INSTANCE.unregisterAllCommands
import net.mamoe.mirai.console.command.SimpleCommand
import net.mamoe.mirai.console.command.UserCommandSender
import net.mamoe.mirai.console.plugin.jvm.JvmPlugin
import net.mamoe.mirai.console.plugin.jvm.JvmPluginDescription
import net.mamoe.mirai.console.plugin.jvm.KotlinPlugin
import java.net.URL
import kotlin.math.abs

@AutoService(JvmPlugin::class)
object Fishpool : KotlinPlugin(
    JvmPluginDescription(
        "net.im45.bot.fishpool",
        "0.2.0"
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
    Fishpool, "errcode"
) {
    private val ERRCODE = URL("https://dev.zapic.cc/err.code")

    @Handler
    suspend fun UserCommandSender.errcode() {
        ERRCODE.readText().apply {
            sendMessage("$this ${MinecraftQuerySocket.pingServer(this, 25565, 2000)}")
        }
    }
}
