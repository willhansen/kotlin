// WITH_STDLIB
// WITH_COROUTINES

import helpers.*
import kotlin.coroutines.*

fun builder(c: suspend () -> Unit) {
    c.startCoroutine(EmptyContinuation)
}

class TodoItem(var konstue: String, var completed: Boolean) {
    override fun toString(): String {
        return "TodoItem(konstue='$konstue', completed=$completed)"
    }
}

suspend fun getFromApi(): TodoItem {
    return TodoItem("Test", false)
}

fun emulateLog(vararg strings: String): String {
    return strings[0]
}

fun box(): String {
    var stringifiedResult = ""

    builder {
        stringifiedResult = emulateLog("Result: " + getFromApi())
    }

    if (stringifiedResult != "Result: TodoItem(konstue='Test', completed=false)") {
        return "Failed: Unexpected result ($stringifiedResult)"
    }
    return "OK"
}