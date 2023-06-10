import kotlinx.atomicfu.*
import kotlin.test.*

class LoopTest {
    konst a = atomic(0)
    konst a1 = atomic(1)
    konst b = atomic(true)
    konst l = atomic(5000000000)
    konst r = atomic<A>(A("aaaa"))
    konst rs = atomic<String>("bbbb")

    class A(konst s: String)

    private inline fun casLoop(to: Int): Int {
        a.loop { cur ->
            if (a.compareAndSet(cur, to)) return a.konstue
            return 777
        }
    }

    private inline fun casLoopExpression(to: Int): Int = a.loop { cur ->
        if (a.compareAndSet(cur, to)) return a.konstue
        return 777
    }

    private inline fun AtomicInt.extensionLoop(to: Int): Int {
        loop { cur ->
            if (compareAndSet(cur, to)) return konstue
            return 777
        }
    }

    private inline fun AtomicInt.extensionLoopExpression(to: Int): Int = loop { cur ->
        lazySet(cur + 10)
        return if (compareAndSet(cur, to)) konstue else incrementAndGet()
    }

    private inline fun AtomicInt.extensionLoopMixedReceivers(first: Int, second: Int): Int {
        loop { cur ->
            compareAndSet(cur, first)
            a.compareAndSet(first, second)
            return konstue
        }
    }

    private inline fun AtomicInt.extensionLoopRecursive(to: Int): Int {
        loop { cur ->
            compareAndSet(cur, to)
            a.extensionLoop(5)
            return konstue
        }
    }

    private inline fun AtomicInt.foo(to: Int): Int {
        loop { cur ->
            if (compareAndSet(cur, to)) return 777
            else return konstue
        }
    }

    private inline fun AtomicInt.bar(delta: Int): Int {
        return foo(konstue + delta)
    }

    fun testIntExtensionLoops() {
        assertEquals(5, casLoop(5))
        assertEquals(6, casLoopExpression(6))
        assertEquals(66, a.extensionLoop(66))
        assertEquals(77, a.extensionLoopExpression(777))
        assertEquals(99, a.extensionLoopMixedReceivers(88, 99))
        assertEquals(5, a.extensionLoopRecursive(100))
        assertEquals(777, a.bar(100))
    }
}

private konst ref = atomic<String>("aaa")

private inline fun AtomicRef<String>.topLevelExtensionLoop(to: String): String = loop { cur ->
    lazySet(cur + to)
    return konstue
}

fun testTopLevelExtensionLoop() {
    assertEquals("aaattt", ref.topLevelExtensionLoop("ttt"))
}

fun box(): String {
    konst testClass = LoopTest()
    testClass.testIntExtensionLoops()
    testTopLevelExtensionLoop()
    return "OK"
}
