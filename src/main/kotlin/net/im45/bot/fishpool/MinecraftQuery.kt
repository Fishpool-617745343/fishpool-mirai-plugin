package net.im45.bot.fishpool

import com.google.gson.Gson
import com.google.gson.internal.LinkedTreeMap
import java.io.*
import java.net.InetSocketAddress
import java.net.Socket
import java.util.*
import java.util.regex.Pattern

object MinecraftQuerySocket {
    private val json = Gson()

    fun pingServer(host: String, port: Int, timeout: Int): StatusResponse? {
        val socket = Socket()
        socket.soTimeout = timeout
        kotlin.runCatching {
            socket.connect(InetSocketAddress(host, port), timeout)
        }.onFailure {
            return null
        }

        val outputStream = socket.getOutputStream()
        val dataOutputStream = DataOutputStream(outputStream)

        val inputStream = socket.getInputStream()

        val b = ByteArrayOutputStream()
        val handshake = DataOutputStream(b)
        handshake.writeByte(0x00)
        writeVarInt(handshake, 578)
        writeVarInt(handshake, host.length)
        handshake.writeBytes(host)
        handshake.writeShort(port)
        writeVarInt(handshake, 1)

        writeVarInt(dataOutputStream, b.size())
        dataOutputStream.write(b.toByteArray())

        dataOutputStream.writeByte(0x01)
        dataOutputStream.writeByte(0x00)
        val dataInputStream = DataInputStream(inputStream)
        readVarInt(dataInputStream)
        var id = readVarInt(dataInputStream)

        if (id == -1) {
            throw IOException("Premature end of stream.")
        }

        if (id != 0x00) {
            throw IOException("Invalid packetID")
        }
        val length = readVarInt(dataInputStream)

        if (length == -1) {
            throw IOException("Premature end of stream.")
        }

        if (length == 0) {
            throw IOException("Invalid string length.")
        }

        val inByte = ByteArray(length)
        dataInputStream.readFully(inByte)
        val text = String(inByte)
        val response = json.fromJson(text, StatusResponse::class.java)

        val now = System.nanoTime()
        dataOutputStream.writeByte(0x09)
        dataOutputStream.writeByte(0x01)
        dataOutputStream.writeLong(now)

        kotlin.runCatching {
            readVarInt(dataInputStream)
        }.onFailure {
            response.time = -1
            dataOutputStream.close()
            outputStream.close()
            dataInputStream.close()
            inputStream.close()
            socket.close()
            return response
        }

        id = readVarInt(dataInputStream)
        if (id == -1) {
            throw IOException("Premature end of stream.")
        }

        if (id != 0x01) {
            throw IOException("Invalid packetID")
        }
        val ping = ((System.nanoTime() - now) / 1000000L).toInt()

        response.time = ping

        dataOutputStream.close()
        outputStream.close()
        dataInputStream.close()
        inputStream.close()
        socket.close()
        return response
    }

    private fun readVarInt(input: DataInputStream): Int {
        var i = 0
        var j = 0
        while (true) {
            val k = input.readByte().toInt()
            i = i or (k and 0x7F shl j++ * 7)
            if (j > 5) throw RuntimeException("ValInt too big")
            if ((k and 0x80) != 128) break
        }
        return i
    }

    private fun writeVarInt(out: DataOutputStream, paramInt: Int) {
        var param = paramInt
        while (true) {
            if (param and -0x80 == 0) {
                out.writeByte(param)
                return
            }
            out.writeByte(param and 0x7F or 0x80)
            param = param ushr 7
        }
    }
}

data class StatusResponse(
    private val description: Any,
    val players: Players,
    val version: Version,
    private val favicon: String?,
    var time: Int
) {
    fun getFavicon(): InputStream? {
        if (favicon == null) return null
        val byteArray = Base64.getDecoder().decode(favicon.trim().split(",")[1])
        return ByteArrayInputStream(byteArray)
    }

    fun getDescription(): String? {
        if (description is LinkedTreeMap<*, *>) {
            if (!description.contains("extra")) {
                return STRIP_COLOR_PATTERN.matcher(description["text"].toString())
                    .replaceAll("")
            }
            val extra = description["extra"]
            val desc = StringBuilder()
            if (extra is ArrayList<*>) {
                for (text in extra) {
                    if (text is LinkedTreeMap<*, *>) {
                        desc.append(text["text"])
                    }
                }
            }
            return desc.toString()
        }
        return STRIP_COLOR_PATTERN.matcher(description.toString())
            .replaceAll("")
    }
}

data class Players(
    val max: Int,
    val online: Int
)

data class Version(
    private val name: String,
    val protocol: Int
) {
    fun getName(): String {
        return STRIP_COLOR_PATTERN.matcher(name)
            .replaceAll("")
    }
}

private val STRIP_COLOR_PATTERN: Pattern = Pattern.compile("(?i)§[0-9A-FK-OR]")
