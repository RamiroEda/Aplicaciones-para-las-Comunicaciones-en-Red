import javafx.geometry.Insets
import javafx.geometry.Pos
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.launch
import tornadofx.*

fun main() {
    val serverJob = GlobalScope.launch {
        ServerTCP()
    }

    launch<ClientApp>()

    serverJob.cancel()
}


class ClientApp : App(ClientWindow::class)

class ClientWindow : View(){
    override val root = vbox {
        alignment = Pos.CENTER
        padding = Insets(16.0)

        button(
            text = "Abrir archivo"
        ) {
            setOnAction {
                val file = chooseFile(
                    title = "Selecciona un archivo",
                    initialDirectory = javax.swing.filechooser.FileSystemView.getFileSystemView().homeDirectory,
                    filters = arrayOf(),
                    mode = FileChooserMode.Single
                ).singleOrNull()

                if (file != null) {
                    val clientTCP = ClientTCP()
                    clientTCP.sendFile(file)
                    clientTCP.close()
                }
            }
        }
    }
}