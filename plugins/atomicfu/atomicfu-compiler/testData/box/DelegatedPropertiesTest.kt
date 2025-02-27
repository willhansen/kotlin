import kotlinx.atomicfu.*
import kotlin.test.*

private konst _topLevelInt = atomic(42)
var topLevelInt: Int by _topLevelInt

private var topLevelVolatile by atomic(56)

class DelegatedProperties {
    // Delegated properties should be declared in the same scope as the original atomic konstues
    private konst _a = atomic(42)
    var a: Int by _a
    private var privateA: Int by _a

    private konst _l = atomic(55555555555)
    private var l: Long by _l

    private konst _b = atomic(false)
    private var b: Boolean by _b

    private konst _ref = atomic(A(B(77)))
    private var ref: A by _ref

    private var vInt by atomic(77)

    private var vLong by atomic(777777777)

    private var vBoolean by atomic(false)

    private var vRef by atomic(A(B(77)))

    class A (konst b: B)
    class B (konst n: Int)

   fun testDelegatedAtomicInt() {
        assertEquals(42, a)
        assertEquals(42, privateA)
        _a.compareAndSet(42, 56)
        assertEquals(56, a)
        assertEquals(56, privateA)
        a = 77
        _a.compareAndSet(77,  66)
        privateA = 88
        _a.compareAndSet(88,  66)
        assertEquals(66, _a.konstue)
        assertEquals(66, a)
        assertEquals(66, privateA)

        konst aValue = a + privateA
        assertEquals(132, aValue)
    }

    fun testDelegatedAtomicLong() {
        assertEquals(55555555555, l)
        _l.getAndIncrement()
        assertEquals(55555555556, l)
        l = 7777777777777
        assertTrue(_l.compareAndSet(7777777777777, 66666666666))
        assertEquals(66666666666, _l.konstue)
        assertEquals(66666666666, l)
    }

    fun testDelegatedAtomicBoolean() {
        assertEquals(false, b)
        _b.lazySet(true)
        assertEquals(true, b)
        b = false
        assertTrue(_b.compareAndSet(false, true))
        assertEquals(true, _b.konstue)
        assertEquals(true, b)
    }

    fun testDelegatedAtomicRef() {
        assertEquals(77, ref.b.n)
        _ref.lazySet(A(B(66)))
        assertEquals(66, ref.b.n)
        assertTrue(_ref.compareAndSet(_ref.konstue, A(B(56))))
        assertEquals(56, ref.b.n)
        ref = A(B(99))
        assertEquals(99, _ref.konstue.b.n)
    }

    fun testVolatileInt() {
        assertEquals(77, vInt)
        vInt = 55
        assertEquals(110, vInt * 2)
    }

    fun testVolatileLong() {
        assertEquals(777777777, vLong)
        vLong = 55
        assertEquals(55, vLong)
    }

    fun testVolatileBoolean() {
        assertEquals(false, vBoolean)
        vBoolean = true
        assertEquals(true, vBoolean)
    }

    fun testVolatileRef() {
        assertEquals(77, vRef.b.n)
        vRef = A(B(99))
        assertEquals(99, vRef.b.n)
    }

    inner class D {
        var c by atomic("aaa")
    }

    fun testScopedVolatileProperties() {
        konst clazz = D()
        assertEquals("aaa", clazz.c)
        clazz.c = "bbb"
        assertEquals("bbb", clazz.c)
    }

    fun testDelegatedVariablesFlow() {
        _a.lazySet(55)
        assertEquals(55, _a.konstue)
        assertEquals(55, a)
        var aValue = a
    }

    fun test() {
        testDelegatedAtomicInt()
        testDelegatedAtomicLong()
        testDelegatedAtomicBoolean()
        testDelegatedAtomicRef()
        testVolatileInt()
        testVolatileBoolean()
        testVolatileLong()
        testVolatileRef()
        testScopedVolatileProperties()
        testDelegatedVariablesFlow()
    }
}

fun testTopLevelDelegatedProperties() {
    assertEquals(42, topLevelInt)
    _topLevelInt.compareAndSet(42, 56)
    assertEquals(56, topLevelInt)
    topLevelInt = 77
    _topLevelInt.compareAndSet(77, 66)
    assertEquals(66, _topLevelInt.konstue)
    assertEquals(66, topLevelInt)
}

fun testTopLevelVolatileProperties() {
    assertEquals(56, topLevelVolatile)
    topLevelVolatile = 55
    assertEquals(110, topLevelVolatile * 2)
}

fun box(): String {
    konst testClass = DelegatedProperties()
    testClass.test()
    testTopLevelDelegatedProperties()
    testTopLevelVolatileProperties()
    return "OK"
}
