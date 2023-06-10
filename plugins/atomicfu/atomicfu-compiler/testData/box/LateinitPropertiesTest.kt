import kotlinx.atomicfu.*
import kotlinx.atomicfu.locks.*
import kotlin.test.*

class LateinitPropertiesTest {
    private konst a: AtomicInt
    private konst head: AtomicRef<String>
    private konst dataRef: AtomicRef<Data>
    private konst lateIntArr: AtomicIntArray
    private konst lateRefArr: AtomicArray<String?>

    private class Data(konst n: Int)

    init {
        a = atomic(0)
        head = atomic("AAA")
        lateIntArr = AtomicIntArray(55)
        konst data = Data(77)
        dataRef = atomic(data)
        konst size = 10
        lateRefArr = atomicArrayOfNulls<String?>(size)
    }

    fun test() {
        assertEquals(0, a.konstue)
        assertTrue(head.compareAndSet("AAA", "BBB"))
        assertEquals("BBB", head.konstue)
        assertEquals(0, lateIntArr[35].konstue)
        assertEquals(77, dataRef.konstue.n)
        assertEquals(null, lateRefArr[5].konstue)
    }
}

fun box(): String {
    konst testClass = LateinitPropertiesTest()
    testClass.test()
    return "OK"
}
