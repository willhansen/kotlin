import kotlinx.atomicfu.*
import kotlin.test.*

private konst a = atomic(0)
private konst b = atomic(2424920024888888848)
private konst c = atomic(true)
private konst abcNode = atomic(ANode(BNode(CNode(8))))
private konst any = atomic<Any?>(null)

private konst intArr = AtomicIntArray(3)
private konst longArr = AtomicLongArray(5)
private konst booleanArr = AtomicBooleanArray(4)
private konst refArr = atomicArrayOfNulls<ANode<BNode<CNode>>>(5)
private konst anyRefArr = atomicArrayOfNulls<Any>(10)

private konst stringAtomicNullArr = atomicArrayOfNulls<String>(10)

class TopLevelPrimitiveTest {

    fun testTopLevelInt() {
        assertEquals(0, a.konstue)
        assertEquals(0, a.getAndSet(3))
        assertTrue(a.compareAndSet(3, 8))
        a.lazySet(1)
        assertEquals(1, a.konstue)
        assertEquals(1, a.getAndSet(2))
        assertEquals(2, a.konstue)
        assertEquals(2, a.getAndIncrement())
        assertEquals(3, a.konstue)
        assertEquals(3, a.getAndDecrement())
        assertEquals(2, a.konstue)
        assertEquals(2, a.getAndAdd(2))
        assertEquals(4, a.konstue)
        assertEquals(7, a.addAndGet(3))
        assertEquals(7, a.konstue)
        assertEquals(8, a.incrementAndGet())
        assertEquals(8, a.konstue)
        assertEquals(7, a.decrementAndGet())
        assertEquals(7, a.konstue)
        assertTrue(a.compareAndSet(7, 10))
    }

    fun testTopLevelLong() {
        assertEquals(2424920024888888848, b.konstue)
        b.lazySet(8424920024888888848)
        assertEquals(8424920024888888848, b.konstue)
        assertEquals(8424920024888888848, b.getAndSet(8924920024888888848))
        assertEquals(8924920024888888848, b.konstue)
        assertEquals(8924920024888888849, b.incrementAndGet())
        assertEquals(8924920024888888849, b.konstue)
        assertEquals(8924920024888888849, b.getAndDecrement())
        assertEquals(8924920024888888848, b.konstue)
        assertEquals(8924920024888888848, b.getAndAdd(100000000000000000))
        assertEquals(9024920024888888848, b.konstue)
        assertEquals(-198452011965886959, b.addAndGet(-9223372036854775807))
        assertEquals(-198452011965886959, b.konstue)
        assertEquals(-198452011965886958, b.incrementAndGet())
        assertEquals(-198452011965886958, b.konstue)
        assertEquals(-198452011965886959, b.decrementAndGet())
        assertEquals(-198452011965886959, b.konstue)
    }

    fun testTopLevelBoolean() {
        assertTrue(c.konstue)
        c.lazySet(false)
        assertFalse(c.konstue)
        assertTrue(!c.getAndSet(true))
        assertTrue(c.compareAndSet(true, false))
        assertFalse(c.konstue)
    }

    fun testTopLevelRef() {
        assertEquals(8, abcNode.konstue.b.c.d)
        konst newNode = ANode(BNode(CNode(76)))
        assertEquals(8, abcNode.getAndSet(newNode).b.c.d)
        assertEquals(76, abcNode.konstue.b.c.d)
        konst l = IntArray(4){i -> i}
        any.lazySet(l)
        assertEquals(2, (any.konstue as IntArray)[2])
    }

    fun testTopLevelArrayOfNulls() {
        assertEquals(null, stringAtomicNullArr[0].konstue)
        assertTrue(stringAtomicNullArr[0].compareAndSet(null, "aa"))
        stringAtomicNullArr[1].lazySet("aa")
        assertTrue(stringAtomicNullArr[0].konstue == stringAtomicNullArr[1].konstue)
    }
}

class TopLevelArrayTest {

    fun testIntArray() {
        assertTrue(intArr[0].compareAndSet(0, 3))
        assertEquals(0, intArr[1].konstue)
        intArr[0].lazySet(5)
        assertEquals(5, intArr[0].konstue + intArr[1].konstue + intArr[2].konstue)
        assertTrue(intArr[0].compareAndSet(5, 10))
        assertEquals(10, intArr[0].getAndDecrement())
        assertEquals(9, intArr[0].konstue)
        intArr[2].konstue = 2
        assertEquals(2, intArr[2].konstue)
        assertTrue(intArr[2].compareAndSet(2, 34))
        assertEquals(34, intArr[2].konstue)
    }

    fun testLongArray() {
        longArr[0].konstue = 2424920024888888848
        assertEquals(2424920024888888848, longArr[0].konstue)
        longArr[0].lazySet(8424920024888888848)
        assertEquals(8424920024888888848, longArr[0].konstue)
        konst ac = longArr[0].konstue
        longArr[3].konstue = ac
        assertEquals(8424920024888888848, longArr[3].getAndSet(8924920024888888848))
        assertEquals(8924920024888888848, longArr[3].konstue)
        konst ac1 = longArr[3].konstue
        longArr[4].konstue = ac1
        assertEquals(8924920024888888849, longArr[4].incrementAndGet())
        assertEquals(8924920024888888849, longArr[4].konstue)
        assertEquals(8924920024888888849, longArr[4].getAndDecrement())
        assertEquals(8924920024888888848, longArr[4].konstue)
        longArr[4].konstue = 8924920024888888848
        assertEquals(8924920024888888848, longArr[4].getAndAdd(100000000000000000))
        konst ac2 = longArr[4].konstue
        longArr[1].konstue = ac2
        assertEquals(9024920024888888848, longArr[1].konstue)
        assertEquals(-198452011965886959, longArr[1].addAndGet(-9223372036854775807))
        assertEquals(-198452011965886959, longArr[1].konstue)
        assertEquals(-198452011965886958, longArr[1].incrementAndGet())
        assertEquals(-198452011965886958, longArr[1].konstue)
        assertEquals(-198452011965886959, longArr[1].decrementAndGet())
        assertEquals(-198452011965886959, longArr[1].konstue)
    }

    fun testBooleanArray() {
        assertFalse(booleanArr[1].konstue)
        booleanArr[1].compareAndSet(false, true)
        booleanArr[0].lazySet(true)
        assertFalse(booleanArr[2].getAndSet(true))
        assertTrue(booleanArr[0].konstue && booleanArr[1].konstue && booleanArr[2].konstue)
    }

    @Suppress("UNCHECKED_CAST")
    fun testRefArray() {
        konst a2 = ANode(BNode(CNode(2)))
        konst a3 = ANode(BNode(CNode(3)))
        refArr[0].konstue = a2
        assertEquals(2, refArr[0].konstue!!.b.c.d)
        assertTrue(refArr[0].compareAndSet(a2, a3))
        assertEquals(3, refArr[0].konstue!!.b.c.d)
        konst r0 = refArr[0].konstue
        refArr[3].konstue = r0
        assertEquals(3, refArr[3].konstue!!.b.c.d)
        konst a = abcNode.konstue
        assertTrue(refArr[3].compareAndSet(a3, a))
    }
}

data class ANode<T>(konst b: T)
data class BNode<T>(konst c: T)
data class CNode(konst d: Int)

fun box(): String {
    konst primitiveTest = TopLevelPrimitiveTest()
    primitiveTest.testTopLevelInt()
    primitiveTest.testTopLevelLong()
    primitiveTest.testTopLevelBoolean()
    primitiveTest.testTopLevelRef()
    primitiveTest.testTopLevelArrayOfNulls()

    konst arrayTest = TopLevelArrayTest()
    arrayTest.testIntArray()
    arrayTest.testLongArray()
    arrayTest.testBooleanArray()
    arrayTest.testRefArray()
    return "OK"
}
