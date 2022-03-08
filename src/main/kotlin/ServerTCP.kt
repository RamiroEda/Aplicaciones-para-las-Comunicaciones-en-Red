import kotlinx.coroutines.Dispatchers
import java.io.File
import java.net.*
import java.nio.ByteBuffer
import java.nio.channels.DatagramChannel
import java.nio.charset.StandardCharsets
import java.nio.file.Path


class ServerTCP {
    init {
        println("Iniciando servidor")
    }

    private val server = MulticastSocket(3000)
    private var isActive = true
    private val clientHistory = ArrayList<SocketAddress>()

    init {
        server.joinGroup(InetAddress.getByName("224.10.10.25"))

        while (isActive){
            receiveFile()
        }
    }

    fun closeServer(){
        isActive = false
        server.close()
        println("Cerrando server")
    }

    private fun receiveFile() = with(Dispatchers.IO){
        val filenameBuffer = ByteArray(1024)
        val fileNamePacket = DatagramPacket(filenameBuffer, filenameBuffer.size)
        server.receive(fileNamePacket)

        val clientId = if(clientHistory.contains(fileNamePacket.socketAddress)){
            clientHistory.indexOf(fileNamePacket.socketAddress)
        }else{
            clientHistory.add(fileNamePacket.socketAddress)
            clientHistory.lastIndex
        }

        println("Recibiendo nombre del archivo desde ${fileNamePacket.socketAddress}")

        val fileName = "cliente${clientId}_${
            String(fileNamePacket.data).trim().filter {
                it.toInt() != 0
            }
        }"

        println("Nombre del archivo recibido: $fileName")

        val fileSizeBuffer = ByteArray(1024)
        val fileSizePacket = DatagramPacket(fileSizeBuffer, fileSizeBuffer.size)
        server.receive(fileSizePacket)
        val fileSize = ByteBuffer.wrap(fileSizePacket.data).long.toInt()

        println("Recibiendo archivo de longitud $fileSize")

        val file = File(Path.of("serverFile", fileName).toUri()).also {
            if(it.exists()){
                println("Archivo ya creado, borrando datos")
                it.delete()
            }

            println("Creando archivo en '${it.absolutePath}'")
            it.createNewFile()
        }

        var currentIndex = 0
        while(currentIndex < fileSize){
            val next = if (currentIndex+1024 > fileSize){
                fileSize
            }else{
                currentIndex+1024
            }
            val fileBuffer = ByteArray(next-currentIndex)
            val filePacket = DatagramPacket(fileBuffer, fileBuffer.size)
            server.receive(filePacket)
            file.appendBytes(filePacket.data)
            currentIndex = next
        }

        for(i in 0 until fileSize){
            val fileBuffer = ByteArray(1024)
            val filePacket = DatagramPacket(fileBuffer, fileBuffer.size, fileNamePacket.socketAddress)
            server.receive(filePacket)
            file.appendBytes(filePacket.data)
        }

        println("Archivo recibido")
    }
}

fun main(){
    ServerTCP()
}