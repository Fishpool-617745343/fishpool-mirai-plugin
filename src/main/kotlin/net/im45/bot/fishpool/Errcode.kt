package net.im45.bot.fishpool

import io.ktor.client.*
import io.ktor.client.features.*
import io.ktor.client.request.*
import io.ktor.http.*
import net.mamoe.mirai.console.command.SimpleCommand
import net.mamoe.mirai.console.command.UserCommandSender
import net.mamoe.mirai.console.data.ReadOnlyPluginConfig
import net.mamoe.mirai.console.data.value
import java.net.InetSocketAddress
import java.net.Socket

object Errcode : SimpleCommand(
    Fishpool, "errcode",
    description = "err.code"
) {
    private val errcode = Url("https://dev.zapic.moe/err.code")
    private val client = HttpClient {
        install(HttpTimeout) {
            connectTimeoutMillis = ErrcodeConfig.connectTimeout
            requestTimeoutMillis = ErrcodeConfig.requestTimeout
            socketTimeoutMillis = ErrcodeConfig.socketTimeout
        }
    }

    private class ZapicException(cause: ServerResponseException) :
        IllegalStateException("${cause.response.status.value}@$errcode", cause)

    private class LJYYSException(opening: Boolean, host: String) :
        Exception(if (opening) host else "Closed@$host")

    private object ErrcodeConfig : ReadOnlyPluginConfig("errcode") {
        val connectTimeout by value(10000L)
        val requestTimeout by value(30000L)
        val socketTimeout by value(10000L)
        val serverTimeout by value(1000)
    }

    /**
     * @see [ThreadGroup.uncaughtException]
     */
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
            sendMessage(
                stackTraceFormatter(if (it is ServerResponseException) ZapicException(it) else it)
            )
        }.onSuccess { ec ->
            Socket().runCatching {
                use { connect(InetSocketAddress(ec, 25565), ErrcodeConfig.serverTimeout) }
            }.let {
                sendMessage(stackTraceFormatter(LJYYSException(it.isSuccess, ec)))
            }
        }
    }
}
