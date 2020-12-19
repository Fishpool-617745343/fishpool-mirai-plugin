package net.im45.bot.fishpool

import com.google.auto.service.AutoService
import io.ktor.client.*
import io.ktor.client.engine.cio.*
import io.ktor.client.features.*
import io.ktor.client.request.*
import net.mamoe.mirai.console.command.CommandManager.INSTANCE.register
import net.mamoe.mirai.console.command.CommandManager.INSTANCE.unregisterAllCommands
import net.mamoe.mirai.console.command.SimpleCommand
import net.mamoe.mirai.console.command.UserCommandSender
import net.mamoe.mirai.console.data.AutoSavePluginConfig
import net.mamoe.mirai.console.data.value
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
        "0.2.2",
        "Fishpool"
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

        unregisterAllCommands(this)
    }
}

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

object Errcode : SimpleCommand(
    Fishpool, "errcode",
    description = "err.code"
) {
    private val errcode = URL("https://dev.zapic.moe/err.code")
    private val client = HttpClient(CIO) {
        install(HttpTimeout) {
            connectTimeoutMillis = ErrcodeConfig.connectTimeout
            requestTimeoutMillis = ErrcodeConfig.requestTimeout
            socketTimeoutMillis = ErrcodeConfig.socketTimeout
        }
    }

    private class LJYYSException(opening: Boolean, host: String) : Exception(if (opening) host else "Closed@$host")

    private object ErrcodeConfig : AutoSavePluginConfig("errcode") {
        val connectTimeout by value(10000L)
        val requestTimeout by value(30000L)
        val socketTimeout by value(10000L)
        val serverTimeout by value(1000)
    }

    private fun stackTraceFormatter(ex: Throwable, limit: Int = 3) = buildString {
        append("Exception in thread \"${Thread.currentThread().name}\": $ex")
        ex.stackTrace.let {
            it.take(limit).forEach { trace -> append("\n\tat $trace") }
            if (it.size > limit) append("\n\t... ${it.size - limit} more")
        }
    }

    @Handler
    suspend fun UserCommandSender.errcode() {
        runCatching {
            client.get<String>(errcode)
        }.onFailure {
            sendMessage(stackTraceFormatter(it))
        }.onSuccess { ec ->
            Socket().runCatching {
                use { connect(InetSocketAddress(ec, 25565), ErrcodeConfig.serverTimeout) }
            }.let {
                sendMessage(stackTraceFormatter(LJYYSException(it.isSuccess, ec)))
            }
        }
    }
}

