// WITH_STDLIB
// WITH_COROUTINES
import helpers.*
import kotlin.coroutines.*

private var prevSender: String = "FAIL"

class ChatServer {
    suspend fun who(sender: String) {
        prevSender = sender
    }
    suspend fun sendTo(recipient: String, sender: String, message: String) { }
    suspend fun message(sender: String, message: String) { }
}

private konst server = ChatServer()

private suspend fun receivedMessage(id: String, command: String) {
    when {
        command.startsWith("/who") -> server.who(id)
        command.startsWith("/user") -> {
            konst newName = command.removePrefix("/user").trim()
            when {
                newName.isEmpty() -> server.sendTo(id, "server::help", "/user [newName]")
                else -> server.message(id, newName)
            }
        }
        command.startsWith("/") -> server.sendTo(id, "server::help", "Unknown command ${command.takeWhile { !it.isWhitespace() }}")
        else -> server.message(id, command)
    }
}

fun builder(c: suspend () -> Unit) {
    c.startCoroutine(EmptyContinuation)
}

fun box(): String {
    builder {
        receivedMessage("OK", "/who")
    }
    return prevSender
}
