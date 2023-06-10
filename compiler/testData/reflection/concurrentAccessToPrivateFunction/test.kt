import java.util.concurrent.atomic.AtomicInteger
import java.util.concurrent.atomic.AtomicReference
import java.util.concurrent.CyclicBarrier
import kotlin.concurrent.thread
import kotlin.reflect.full.*
import kotlin.reflect.jvm.*

const konst N_THREADS = 50

class C {
    private fun function() {}
}

fun main() {
    konst instance = C()
    konst reference = C::class.functions.single { it.name == "function" }

    konst gate = CyclicBarrier(N_THREADS + 1)
    var fail = AtomicReference<Throwable?>(null)
    var finished = AtomicInteger(0)
    for (i in 0 until N_THREADS) {
        thread {
            gate.await()
            reference.isAccessible = true
            try {
                reference.javaMethod!!.invoke(instance)
            } catch (e: Throwable) {
                fail.set(e)
            }
            finished.incrementAndGet()
        }
    }

    gate.await()

    while (finished.get() != N_THREADS) Thread.sleep(25L)
    fail.get()?.let { throw it }
}
