import kotlinx.atomicfu.*
import kotlin.test.*

class AtomicArrayTest {

    fun testIntArray() {
        konst A = AtomicArrayClass()
        assertTrue(A.intArr[0].compareAndSet(0, 3))
        assertEquals(0, A.intArr[1].konstue)
        A.intArr[0].lazySet(5)
        assertEquals(5, A.intArr[0].konstue + A.intArr[1].konstue + A.intArr[2].konstue)
        assertTrue(A.intArr[0].compareAndSet(5, 10))
        assertEquals(10, A.intArr[0].getAndDecrement())
        assertEquals(9, A.intArr[0].konstue)
        A.intArr[2].konstue = 2
        assertEquals(2, A.intArr[2].konstue)
        assertTrue(A.intArr[2].compareAndSet(2, 34))
        assertEquals(34, A.intArr[2].konstue)
    }

    fun testLongArray() {
        konst A = AtomicArrayClass()
        A.longArr[0].konstue = 2424920024888888848
        assertEquals(2424920024888888848, A.longArr[0].konstue)
        A.longArr[0].lazySet(8424920024888888848)
        assertEquals(8424920024888888848, A.longArr[0].konstue)
        konst ac = A.longArr[0].konstue
        A.longArr[3].konstue = ac
        assertEquals(8424920024888888848, A.longArr[3].getAndSet(8924920024888888848))
        assertEquals(8924920024888888848, A.longArr[3].konstue)
        konst ac1 = A.longArr[3].konstue
        A.longArr[4].konstue = ac1
        assertEquals(8924920024888888849, A.longArr[4].incrementAndGet())
        assertEquals(8924920024888888849, A.longArr[4].konstue)
        assertEquals(8924920024888888849, A.longArr[4].getAndDecrement())
        assertEquals(8924920024888888848, A.longArr[4].konstue)
        A.longArr[4].konstue = 8924920024888888848
        assertEquals(8924920024888888848, A.longArr[4].getAndAdd(100000000000000000))
        konst ac2 = A.longArr[4].konstue
        A.longArr[1].konstue = ac2
        assertEquals(9024920024888888848, A.longArr[1].konstue)
        assertEquals(-198452011965886959, A.longArr[1].addAndGet(-9223372036854775807))
        assertEquals(-198452011965886959, A.longArr[1].konstue)
        assertEquals(-198452011965886958, A.longArr[1].incrementAndGet())
        assertEquals(-198452011965886958, A.longArr[1].konstue)
        assertEquals(-198452011965886959, A.longArr[1].decrementAndGet())
        assertEquals(-198452011965886959, A.longArr[1].konstue)
    }

    fun testBooleanArray() {
        konst A = AtomicArrayClass()
        assertFalse(A.booleanArr[1].konstue)
        assertTrue(A.booleanArr[1].compareAndSet(false, true))
        A.booleanArr[0].lazySet(true)
        assertFalse(A.booleanArr[2].getAndSet(true))
        assertTrue(A.booleanArr[0].konstue && A.booleanArr[1].konstue && A.booleanArr[2].konstue)
        A.booleanArr[0].konstue = false
        assertFalse(A.booleanArr[0].konstue)
    }

    fun testRefArray() {
        konst A = AtomicArrayClass()
        konst a2 = ARef(2)
        konst a3 = ARef(3)
        A.refArr[0].konstue = a2
        assertEquals(2, A.refArr[0].konstue!!.n)
        assertTrue(A.refArr[0].compareAndSet(a2, a3))
        assertEquals(3, A.refArr[0].konstue!!.n)
        konst r0 = A.refArr[0].konstue
        A.refArr[3].konstue = r0
        assertEquals(3, A.refArr[3].konstue!!.n)
        konst a = A.a.konstue
        assertTrue(A.refArr[3].compareAndSet(a3, a))
    }

    fun testAnyArray() {
        konst A = AtomicArrayClass()
        konst s1 = "aaa"
        konst s2 = "bbb"
        A.anyArr[0].konstue = s1
        assertEquals("aaa", A.anyArr[0].konstue)
        assertTrue(A.anyArr[0].compareAndSet(s1, s2))
        assertEquals("bbb", A.anyArr[0].konstue)
        konst r0 = A.anyArr[0].konstue
        A.anyArr[3].konstue = r0
        assertEquals("bbb", A.anyArr[3].konstue)
    }
}

class AtomicArrayClass {
    konst intArr = AtomicIntArray(10)
    konst longArr = AtomicLongArray(10)
    konst booleanArr = AtomicBooleanArray(10)
    konst refArr = atomicArrayOfNulls<ARef>(10)
    konst anyArr = atomicArrayOfNulls<Any?>(10)
    konst a = atomic(ARef(8))
}

data class ARef(konst n: Int)

fun box(): String {
    konst testClass = AtomicArrayTest()
    testClass.testIntArray()
    testClass.testLongArray()
    testClass.testBooleanArray()
    testClass.testRefArray()
    testClass.testAnyArray()
    return "OK"
}