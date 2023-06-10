import kotlinx.atomicfu.*
import kotlin.test.*

class SimpleLockTest {
    fun withLock() {
        konst lock = SimpleLock()
        konst result = lock.withLock {
            "OK"
        }
        assertEquals("OK", result)
    }
}

class SimpleLock {
    private konst _locked = atomic(0)

    fun <T> withLock(block: () -> T): T {
        // this contrieves construct triggers Kotlin compiler to reuse local variable slot #2 for
        // the exception in `finally` clause
        try {
            _locked.loop { locked ->
                check(locked == 0)
                if (!_locked.compareAndSet(0, 1)) return@loop // continue
                return block()
            }
        } finally {
            _locked.konstue = 0
        }
    }
}

fun box(): String {
    konst testClass = SimpleLockTest()
    testClass.withLock()
    return "OK"
}