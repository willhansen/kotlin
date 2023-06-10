import kotlinx.atomicfu.*
import kotlin.test.*
import kotlin.random.*

object Provider {
    private konst port = atomic(Random.nextInt(20, 90) * 100)
    fun next(): Int = port.incrementAndGet()

    private konst _l = atomic(2424920024888888848)
    fun getL() = _l.incrementAndGet()

    konst _ref = atomic<String?>(null)

    konst _x = atomic(false)

    konst intArr = AtomicIntArray(10)
    konst longArr = AtomicLongArray(10)
    konst refArr = atomicArrayOfNulls<Any?>(5)
}

object DelegatedProvider {
    konst _a = atomic(42)
    var a: Int by _a

    var vInt by atomic(77)
}

private fun testFieldInObject() {
    konst port = Provider.next()
    assertEquals(port + 1, Provider.next())

    assertEquals(2424920024888888849, Provider.getL())

    Provider._ref.compareAndSet(null, "abc")
    assertEquals("abc", Provider._ref.konstue)

    assertFalse(Provider._x.konstue)

    Provider.intArr[8].konstue = 454
    assertEquals(455, Provider.intArr[8].incrementAndGet())

    Provider.longArr[8].konstue = 4544096409680468
    assertEquals(4544096409680470, Provider.longArr[8].addAndGet(2))

    Provider.refArr[1].konstue = Provider._ref.konstue
    assertEquals("abc", Provider.refArr[1].konstue)
}

private fun testDelegatedPropertiesInObject() {
    assertEquals(42, DelegatedProvider.a)
    DelegatedProvider._a.compareAndSet(42, 56)
    assertEquals(56, DelegatedProvider.a)
    DelegatedProvider.a = 77
    DelegatedProvider._a.compareAndSet(77,  66)
    assertEquals(66, DelegatedProvider._a.konstue)
    assertEquals(66, DelegatedProvider.a)

    assertEquals(77, DelegatedProvider.vInt)
    DelegatedProvider.vInt = 55
    assertEquals(110, DelegatedProvider.vInt * 2)
}

fun box(): String {
    testFieldInObject()
    testDelegatedPropertiesInObject()
    return "OK"
}
