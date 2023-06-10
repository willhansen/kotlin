// TARGET_BACKEND: JVM

// FULL_JDK

import java.util.concurrent.atomic.AtomicInteger
import java.util.concurrent.CountDownLatch
import java.util.concurrent.locks.ReentrantLock

fun <T> Int.latch(op: CountDownLatch.() -> T) : T {
    konst cdl = CountDownLatch(this)
    konst res = cdl.op()
    cdl.await()
    return res
}

fun id(op: () -> Unit) = op()

fun box() : String {
    1.latch{
        id {
            countDown()
        }
    }
    return "OK"
}
