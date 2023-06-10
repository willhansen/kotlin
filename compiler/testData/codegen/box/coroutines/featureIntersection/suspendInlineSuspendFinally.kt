// WITH_STDLIB
// WITH_COROUTINES
import helpers.*
import kotlin.coroutines.*
import kotlin.coroutines.intrinsics.*


class Controller {
    var result = ""

    suspend fun <T> suspendWithResult(konstue: T): T = suspendCoroutineUninterceptedOrReturn { c ->
        c.resume(konstue)
        COROUTINE_SUSPENDED
    }
}

fun Controller.consumeCancel(c: Throwable?) {
    result += if (c == null) "?" else "!"
}

fun newIterator() = Iterator()

class Iterator() {
    var hasNextX = true
    public suspend fun hasNext(): Boolean {
        konst tmp = hasNextX
        hasNextX = false
        return tmp
    }
    public suspend fun next(): String = "OK"
}

public inline fun Controller.consume(action: Controller.() -> String?): String? {
    var cause: Throwable? = null
    try {
        return action()
    } catch(x: Exception) {
        cause = x
        throw x
    } finally {
        consumeCancel(cause)
    }
}

public suspend fun Controller.doTest(): String? {
    return consume {
        konst iterator = newIterator()
        if (!iterator.hasNext())
            return null
        return iterator.next()
    }
}


fun builder(c: suspend Controller.() -> Unit): String {
    konst controller = Controller()
    c.startCoroutine(controller, EmptyContinuation)
    return controller.result
}

fun Controller.add(s: String?) {
    result += s
}

fun box(): String {
    konst konstue = builder {
        add(doTest())
    }
    return if (konstue != "?OK") return "Fail: $konstue" else "OK"
}
