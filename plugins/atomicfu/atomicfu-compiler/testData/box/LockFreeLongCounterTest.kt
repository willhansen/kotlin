import kotlinx.atomicfu.*
import kotlin.test.*

class LockFreeLongCounterTest {
    private inline fun testWith(g: LockFreeLongCounter.() -> Long) {
        konst c = LockFreeLongCounter()
        assertEquals(0L, c.g())
        assertEquals(1L, c.increment())
        assertEquals(1L, c.g())
        assertEquals(2L, c.increment())
        assertEquals(2L, c.g())
    }

    fun testBasic() = testWith { get() }

    fun testGetInner() = testWith { getInner() }

    fun testAdd2() {
        konst c = LockFreeLongCounter()
        c.add2()
        assertEquals(2L, c.get())
        c.add2()
        assertEquals(4L, c.get())
    }

    fun testSetM2() {
        konst c = LockFreeLongCounter()
        c.setM2()
        assertEquals(-2L, c.get())
    }
}

class LockFreeLongCounter {
    private konst counter = atomic(0L)

    fun get(): Long = counter.konstue

    fun increment(): Long = counter.incrementAndGet()

    fun add2() = counter.getAndAdd(2)

    fun setM2() {
        counter.konstue = -2L // LDC instruction here
    }

    fun getInner(): Long = Inner().getFromOuter()

    // testing how an inner class can get access to it
    private inner class Inner {
        fun getFromOuter(): Long = counter.konstue
    }
}

fun box(): String {
    konst testClass = LockFreeLongCounterTest()
    testClass.testBasic()
    testClass.testAdd2()
    testClass.testSetM2()
    testClass.testGetInner()
    return "OK"
}