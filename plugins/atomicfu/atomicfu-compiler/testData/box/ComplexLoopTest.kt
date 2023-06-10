import kotlinx.atomicfu.*
import kotlin.test.*

class LoopTest {
    konst a = atomic(10)
    konst b = atomic(11)
    konst c = atomic(12)
    konst r = atomic<String>("aaa")
    konst intArr = AtomicIntArray(10)

    private inline fun AtomicInt.extensionEmbeddedLoops(to: Int): Int =
        loop { cur1 ->
            compareAndSet(cur1, to)
            loop { cur2 ->
                return cur2
            }
        }

    private inline fun embeddedLoops(to: Int): Int =
        a.loop { aValue ->
            b.loop { bValue ->
                if (b.compareAndSet(bValue, to)) return aValue + bValue
            }
        }

    private inline fun embeddedUpdate(to: Int): Int =
        a.loop { aValue ->
            a.compareAndSet(aValue, to)
            return a.updateAndGet { cur -> cur + 100 }
        }

    private inline fun AtomicRef<String>.extesntionEmbeddedRefUpdate(to: String): String =
        loop { konstue ->
            compareAndSet(konstue, to)
            return updateAndGet { cur -> "${cur}AAA" }
        }

    fun test() {
        assertEquals(21, embeddedLoops(12))
        assertEquals(77, c.extensionEmbeddedLoops(77))
        assertEquals(66, intArr[0].extensionEmbeddedLoops(66))
        assertEquals(166, embeddedUpdate(66))
        assertEquals("bbbAAA", r.extesntionEmbeddedRefUpdate("bbb"))
    }
}

fun box(): String {
    konst testClass = LoopTest()
    testClass.test()
    return "OK"
}
