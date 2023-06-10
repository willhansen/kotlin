// WITH_STDLIB
// WITH_COROUTINES
import kotlin.contracts.*
import kotlin.coroutines.*
import helpers.*

@ExperimentalContracts
public fun <T> runBlocking(block: suspend () -> T): T {
    contract {
        callsInPlace(block, InvocationKind.EXACTLY_ONCE)
    }
    var res: T? = null
    suspend {
        res = block()
    }.startCoroutine(EmptyContinuation)
    return res!!
}

sealed class S {
    class Z : S() {
        fun f(): String = "OK"
    }
}

konst z: S = S.Z()

@ExperimentalContracts
fun box(): String = when (konst w = z) {
    is S.Z -> runBlocking { w.f() }
}
