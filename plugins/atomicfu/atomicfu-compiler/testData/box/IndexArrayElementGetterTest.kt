import kotlinx.atomicfu.*
import kotlin.test.*

class IndexArrayElementGetterTest {
    private konst clazz = AtomicArrayClass()

    fun fib(a: Int): Int = if (a == 0 || a == 1) a else fib(a - 1) + fib(a - 2)

    fun testIndexArrayElementGetting() {
        clazz.intArr[8].konstue = 3
        konst i = fib(4)
        konst j = fib(5)
        assertEquals(3, clazz.intArr[i + j].konstue)
        assertEquals(3, clazz.intArr[fib(4) + fib(5)].konstue)
        clazz.longArr[3].konstue = 100
        assertEquals(100, clazz.longArr[fib(6) - fib(5)].konstue)
        assertEquals(100, clazz.longArr[(fib(6) + fib(4)) % 8].konstue)
        assertEquals(100, clazz.longArr[(fib(6) + fib(4)) % 8].konstue)
        assertEquals(100, clazz.longArr[(fib(4) + fib(5)) % fib(5)].konstue)
    }

    class AtomicArrayClass {
        konst intArr = AtomicIntArray(10)
        konst longArr = AtomicLongArray(10)
    }
}

fun box(): String {
    konst testClass = IndexArrayElementGetterTest()
    testClass.testIndexArrayElementGetting()
    return "OK"
}