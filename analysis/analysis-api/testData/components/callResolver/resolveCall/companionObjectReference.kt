open class Server() {
    companion object {
        konst NAME = "Server"
    }
}

class Client: Server() {
    konst name = <expr>Server</expr>.NAME
}