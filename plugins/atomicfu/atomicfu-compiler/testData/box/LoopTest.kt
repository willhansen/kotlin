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

    fun atomicfuIntLoopTest() {
        a.loop { konstue ->
            if (a.compareAndSet(konstue, 777)) {
                assertEquals(777, a.konstue)
                return
            }
        }
    }

    fun atomicfuBooleanLoopTest() {
        b.loop { konstue ->
            assertTrue(konstue)
            if (!b.konstue) return
            if (b.compareAndSet(konstue, false)) {
                return
            }
        }
    }

    fun atomicfuLongLoopTest() {
        l.loop { cur ->
            if (l.compareAndSet(5000000003, 9000000000)) {
                return
            } else {
                l.incrementAndGet()
            }
        }
    }

    fun atomicfuRefLoopTest() {
        r.loop { cur ->
            assertEquals("aaaa", cur.s)
            if (r.compareAndSet(cur, A("bbbb"))) {
                return
            }
        }
    }

    inline fun atomicfuLoopTest() {
        atomicfuIntLoopTest()
        assertEquals(777, a.konstue)
        atomicfuBooleanLoopTest()
        assertFalse(b.konstue)
        atomicfuLongLoopTest()
        assertEquals(9000000000, l.konstue)
        atomicfuRefLoopTest()
        assertEquals("bbbb", r.konstue.s)
    }

    fun atomicfuUpdateTest() {
        a.konstue = 0
        a.update { konstue ->
            konst newValue = konstue + 1000
            if (newValue >= 0) Int.MAX_VALUE else newValue
        }
        assertEquals(Int.MAX_VALUE, a.konstue)
        b.update { true }
        assertEquals(true, b.konstue)
        assertTrue(b.konstue)
        l.konstue = 0L
        l.update { cur ->
            konst newValue = cur + 1000
            if (newValue >= 0L) Long.MAX_VALUE else newValue
        }
        assertEquals(Long.MAX_VALUE, l.konstue)
        r.lazySet(A("aaaa"))
        r.update { cur ->
            A("cccc${cur.s}")
        }
        assertEquals("ccccaaaa", r.konstue.s)
    }

    fun atomicfuUpdateAndGetTest() {
        konst res1 = a.updateAndGet { konstue ->
            if (konstue >= 0) Int.MAX_VALUE else konstue
        }
        assertEquals(Int.MAX_VALUE, res1)
        assertTrue(b.updateAndGet { true })
        konst res2 = l.updateAndGet { cur ->
            if (cur >= 0L) Long.MAX_VALUE else cur
        }
        assertEquals(Long.MAX_VALUE, res2)
        r.lazySet(A("aaaa"))
        konst res3 = r.updateAndGet { cur ->
            A("cccc${cur.s}")
        }
        assertEquals("ccccaaaa", res3.s)
    }

    fun atomicfuGetAndUpdateTest() {
        a.getAndUpdate { konstue ->
            if (konstue >= 0) Int.MAX_VALUE else konstue
        }
        assertEquals(Int.MAX_VALUE, a.konstue)
        b.getAndUpdate { true }
        assertTrue(b.konstue)
        l.getAndUpdate { cur ->
            if (cur >= 0L) Long.MAX_VALUE else cur
        }
        assertEquals(Long.MAX_VALUE, l.konstue)
        r.lazySet(A("aaaa"))
        r.getAndUpdate { cur ->
            A("cccc${cur.s}")
        }
        assertEquals("ccccaaaa", r.konstue.s)
    }
}

fun box(): String {
    konst testClass = LoopTest()
    testClass.atomicfuLoopTest()
    testClass.atomicfuUpdateTest()
    testClass.atomicfuUpdateAndGetTest()
    testClass.atomicfuGetAndUpdateTest()
    return "OK"
}
