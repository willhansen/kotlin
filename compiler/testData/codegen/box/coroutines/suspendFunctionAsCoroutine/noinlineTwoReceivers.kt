// WITH_STDLIB
// WITH_COROUTINES
import helpers.*
import kotlin.coroutines.*
import kotlin.coroutines.intrinsics.COROUTINE_SUSPENDED
import kotlin.coroutines.intrinsics.suspendCoroutineUninterceptedOrReturn

class MyTest {
    suspend fun act(konstue: String): String = suspendCoroutineUninterceptedOrReturn {
        it.resume(konstue)
        COROUTINE_SUSPENDED
    }
}

suspend fun <T> testAsync(routine: suspend MyTest.() -> T): T = routine(MyTest())

suspend fun Iterable<String>.test() = testAsync {
    var sum = ""
    for (v in this@test) {
        sum += act(v)
    }
    sum
}

fun builder(x: suspend () -> Unit) {
    x.startCoroutine(EmptyContinuation)
}

fun box(): String {
    var res = ""
    builder {
        res = listOf("O", "K").test()
    }

    return res
}
