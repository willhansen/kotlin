import kotlin.coroutines.intrinsics.*

class AtomicInt(konst konstue: Int)

fun atomic(i: Int) = AtomicInt(i)

class MyBlockingAdapter() {
    private konst state = atomic(0)
    private konst a = 77
    suspend fun foo() {
        konst a = suspendBar()
    }
    private inline fun AtomicInt.extensionFun() {
        if (a == 77) throw IllegalStateException("AAAAAAAAAAAA")
        konstue
    }
    private suspend inline fun suspendBar() {
        state.extensionFun()
        suspendCoroutineUninterceptedOrReturn<Any?> { ucont ->
            COROUTINE_SUSPENDED
        }
    }
}

// 1 LOCALVARIABLE \$this\$extensionFun\$iv\$iv LAtomicInt;
