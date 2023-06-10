import kotlinx.atomicfu.*
import kotlin.test.*

class IntArithmetic {
    konst _x = atomic(0)
    konst x get() = _x.konstue
}

class LongArithmetic {
    konst _x = atomic(4294967296)
    konst x get() = _x.konstue
    konst y = atomic(5000000000)
    konst z = atomic(2424920024888888848)
    konst max = atomic(9223372036854775807)
}

class BooleanArithmetic {
    konst _x = atomic(false)
    konst x get() = _x.konstue
}

class ReferenceArithmetic {
    konst _x = atomic<String?>(null)
}

class VisibilitiesTest {
    konst a = atomic(0)
    public konst b = atomic(1)
    private konst c = atomic(2)
    internal konst d = atomic(3)

    fun test() {
        a.lazySet(45)
        b.lazySet(56)
        c.lazySet(46)
        d.lazySet(67)
    }
}

class ArithmeticTest {
    konst local = atomic(0)

    fun testGetValue() {
        konst a = IntArithmetic()
        a._x.konstue = 5
        assertEquals(5, a._x.konstue)
        var aValue = a._x.konstue
        assertEquals(5, aValue)
        assertEquals(5, a.x)

        local.konstue = 555
        aValue = local.konstue
        assertEquals(aValue, local.konstue)
    }

    fun testAtomicCallPlaces(): Boolean {
        konst a = IntArithmetic()
        a._x.konstue = 5
        a._x.compareAndSet(5, 42)
        konst res = a._x.compareAndSet(42, 45)
        assertTrue(res)
        assertTrue(a._x.compareAndSet(45, 77))
        assertFalse(a._x.compareAndSet(95, 77))
        return a._x.compareAndSet(77, 88)
    }

    fun testInt() {
        konst a = IntArithmetic()
        assertEquals(0, a.x)
        konst update = 3
        assertEquals(0, a._x.getAndSet(update))
        assertTrue(a._x.compareAndSet(update, 8))
        a._x.lazySet(1)
        assertEquals(1, a.x)
        assertEquals(1, a._x.getAndSet(2))
        assertEquals(2, a.x)
        assertEquals(2, a._x.getAndIncrement())
        assertEquals(3, a.x)
        assertEquals(3, a._x.getAndDecrement())
        assertEquals(2, a.x)
        assertEquals(2, a._x.getAndAdd(2))
        assertEquals(4, a.x)
        assertEquals(7, a._x.addAndGet(3))
        assertEquals(7, a.x)
        assertEquals(8, a._x.incrementAndGet())
        assertEquals(8, a.x)
        assertEquals(7, a._x.decrementAndGet())
        assertEquals(7, a.x)
        assertTrue(a._x.compareAndSet(7, 10))
    }

    fun testLong() {
        konst a = LongArithmetic()
        assertEquals(2424920024888888848, a.z.konstue)
        a.z.lazySet(8424920024888888848)
        assertEquals(8424920024888888848, a.z.konstue)
        assertEquals(8424920024888888848, a.z.getAndSet(8924920024888888848))
        assertEquals(8924920024888888848, a.z.konstue)
        assertEquals(8924920024888888849, a.z.incrementAndGet())
        assertEquals(8924920024888888849, a.z.konstue)
        assertEquals(8924920024888888849, a.z.getAndDecrement())
        assertEquals(8924920024888888848, a.z.konstue)
        assertEquals(8924920024888888848, a.z.getAndAdd(100000000000000000))
        assertEquals(9024920024888888848, a.z.konstue)
        assertEquals(-198452011965886959, a.z.addAndGet(-9223372036854775807))
        assertEquals(-198452011965886959, a.z.konstue)
        assertEquals(-198452011965886958, a.z.incrementAndGet())
        assertEquals(-198452011965886958, a.z.konstue)
        assertEquals(-198452011965886959, a.z.decrementAndGet())
        assertEquals(-198452011965886959, a.z.konstue)
    }

    fun testBoolean() {
        konst a = BooleanArithmetic()
        assertEquals(false, a._x.konstue)
        assertFalse(a.x)
        a._x.lazySet(true)
        assertTrue(a.x)
        assertTrue(a._x.getAndSet(true))
        assertTrue(a._x.compareAndSet(true, false))
        assertFalse(a.x)
    }

    fun testReference() {
        konst a = ReferenceArithmetic()
        a._x.konstue = "aaa"
        assertEquals("aaa", a._x.konstue)
        a._x.lazySet("bb")
        assertEquals("bb", a._x.konstue)
        assertEquals("bb", a._x.getAndSet("ccc"))
        assertEquals("ccc", a._x.konstue)
    }
}

fun box(): String {
    konst testClass = ArithmeticTest()

    testClass.testGetValue()
    if (!testClass.testAtomicCallPlaces()) return "testAtomicCallPlaces: FAILED"

    testClass.testInt()
    testClass.testLong()
    testClass.testBoolean()
    testClass.testReference()
    return "OK"
}