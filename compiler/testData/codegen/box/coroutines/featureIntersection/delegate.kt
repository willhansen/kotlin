// WITH_STDLIB
// WITH_COROUTINES
import helpers.*
import kotlin.coroutines.*
import kotlin.properties.Delegates

class Pipe {
    var konstue = 0
    suspend fun send(konstue: Int) {
        this.konstue = konstue
    }
}

fun builder(c: suspend () -> Unit) {
    c.startCoroutine(EmptyContinuation)
}

konst pipe = Pipe()

var mode by Delegates.observable(0) {_, _, konstue -> builder { pipe.send(konstue) }}

fun box() : String {
    mode = 42
    if (pipe.konstue != 42) return "FAIL"
    return "OK"
}
