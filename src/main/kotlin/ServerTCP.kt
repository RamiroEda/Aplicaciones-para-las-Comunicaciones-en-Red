import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import java.io.File
import java.nio.channels.ServerSocketChannel
import java.net.InetSocketAddress
import java.io.RandomAccessFile
import java.nio.ByteBuffer
import java.nio.channels.SocketChannel
import java.nio.charset.Charset
import java.nio.charset.StandardCharsets


class ServerTCP {
    init {
        println("Iniciando servidor")
    }

    private val server = ServerSocketChannel.open()
    private var isActive = true

    init {
        server.configureBlocking(false)
        server.bind(InetSocketAddress(9001))

        while (isActive){
            val socketChannel = server.accept()

            if(socketChannel != null){
                println("Cliente en ${socketChannel.remoteAddress}")

                receiveFile(socketChannel)

                socketChannel.close()
            }
        }
    }

    fun closeServer(){
        isActive = false
        server.close()
        println("Cerrando server")
    }

    private fun receiveFile(client: SocketChannel) = with(Dispatchers.IO){
        val buffer = ByteBuffer.allocate(1024)

        println("Recibiendo nombre del archivo")

        client.read(buffer)
        buffer.flip()
        val fileName = StandardCharsets.UTF_8.decode(buffer)
        buffer.clear()

        println("Nombre del archivo recibido: $fileName")

        println("Recibiendo archivo")

        val file = File("serverFile/${fileName}").also {
            println("Creando archivo en ${it.absolutePath}")
            it.createNewFile()
        }

        val fileChannel = RandomAccessFile(file, "rw").channel

        while (client.read(buffer) > 0) {
            buffer.flip()
            fileChannel.write(buffer)
            buffer.clear()
        }
        fileChannel.close()
        println("Archivo recibido")
    }
}

fun main(){
    ServerTCP()
}