import java.io.*
import java.nio.ByteBuffer
import java.nio.channels.SocketChannel
import java.io.RandomAccessFile
import java.net.DatagramPacket
import java.net.InetAddress
import java.net.InetSocketAddress
import java.net.MulticastSocket
import java.nio.CharBuffer
import java.nio.channels.DatagramChannel
import java.nio.charset.Charset
import java.nio.charset.StandardCharsets
import kotlin.math.ceil


class ClientTCP {
    init {
        println("Iniciando cliente")
    }

    private val client = MulticastSocket(3000)
    private val address = InetAddress.getByName("224.10.10.25")

    init {
        client.joinGroup(address)
    }

    fun close(){
        println("Cerrando cliente")
        client.close()
        println("Cliente cerrado")
    }

    private fun Long.bytes() : ByteArray = ByteBuffer.allocate(java.lang.Long.BYTES).putLong(this).array()

    fun sendFile(file: File){
        println("Enviando...")

        println("Enviando nombre del archivo...")
        val fileNameByteArray = file.name.toByteArray(StandardCharsets.UTF_8)
        val fileNamePacket = DatagramPacket(fileNameByteArray, fileNameByteArray.size, address, 3000)
        client.send(fileNamePacket)
        println("Nombre enviado")

        val fileBytes = file.readBytes()
        val packetBytes = ArrayList<ByteArray>()

        var currentIndex = 0
        while(currentIndex < fileBytes.size){
            val next = if (currentIndex+1024 > fileBytes.size){
                fileBytes.size
            }else{
                currentIndex+1024
            }
            packetBytes.add(fileBytes.slice(currentIndex until next).toByteArray())
            currentIndex = next
        }

        println("Enviando longitud: ${fileBytes.size}")

        val fileSizeByteArray = fileBytes.size.toLong().bytes()
        val fileSizePacket = DatagramPacket(fileSizeByteArray, fileSizeByteArray.size)
        client.send(fileSizePacket)

        println("Enviando archivo...")

        for (packetByteArray in packetBytes){
            val filePacket = DatagramPacket(packetByteArray, packetByteArray.size)
            client.send(filePacket)
        }

        println("Archivo enviado")
    }
}

fun main(){
    ClientTCP()
}