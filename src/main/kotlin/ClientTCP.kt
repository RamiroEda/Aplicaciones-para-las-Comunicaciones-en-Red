import java.io.*
import java.nio.ByteBuffer
import java.nio.channels.SocketChannel
import java.io.RandomAccessFile
import java.net.InetSocketAddress
import java.nio.CharBuffer
import java.nio.charset.Charset
import java.nio.charset.StandardCharsets


class ClientTCP {
    init {
        println("Iniciando cliente")
    }

    private val client = SocketChannel.open()

    init {
        client.connect(InetSocketAddress("127.0.0.1", 9001))
    }

    fun close(){
        println("Cerrando cliente")
        client.close()
        println("Cliente cerrado")
    }

    fun sendFile(file: File){
        println("Enviando...")

        println("Enviando nombre del archivo...")
        client.write(StandardCharsets.UTF_8.encode(file.name))
        println("Nombre enviado")

        println("Enviando archivo...")
        val randomAccessFile = RandomAccessFile(file, "rw").channel
        val buffer = ByteBuffer.allocate(1024)

        while (randomAccessFile.read(buffer) > 0) {
            buffer.flip()
            client.write(buffer)
            buffer.clear()
        }

        randomAccessFile.close()
        println("Archivo enviado")
    }
}

fun main(){
    ClientTCP()
}