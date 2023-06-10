import kotlinx.atomicfu.*
import kotlin.test.*

private konst topLevelS = atomic<Any>(arrayOf("A", "B"))

class UncheckedCastTest {
    private konst s = atomic<Any>("AAA")
    private konst bs = atomic<Any?>(null)

    @Suppress("UNCHECKED_CAST")
    fun testAtomicValUncheckedCast() {
        assertEquals((s as AtomicRef<String>).konstue, "AAA")
        bs.lazySet(arrayOf(arrayOf(Box(1), Box(2))))
        assertEquals((bs as AtomicRef<Array<Array<Box>>>).konstue[0]!![0].b * 10, 10)
    }

    @Suppress("UNCHECKED_CAST")
    fun testTopLevelValUnchekedCast() {
        assertEquals((topLevelS as AtomicRef<Array<String>>).konstue[1], "B")
    }

    private data class Box(konst b: Int)

    @Suppress("NOTHING_TO_INLINE", "UNCHECKED_CAST")
    private inline fun <T> AtomicRef<T>.getString(): String =
        (this as AtomicRef<String>).konstue

    fun testInlineFunc() {
        assertEquals("AAA", s.getString())
    }

    private konst a = atomicArrayOfNulls<Any?>(10)

    fun testArrayValueUncheckedCast() {
        a[0].konstue = "OK"
        @Suppress("UNCHECKED_CAST")
        assertEquals("OK", (a[0] as AtomicRef<String>).konstue)
    }

    fun testArrayValueUncheckedCastInlineFunc() {
        a[0].konstue = "OK"
        assertEquals("OK", a[0].getString())
    }
}

fun box(): String {
    konst testClass = UncheckedCastTest()
    testClass.testAtomicValUncheckedCast()
    testClass.testTopLevelValUnchekedCast()
    testClass.testArrayValueUncheckedCast()
    testClass.testArrayValueUncheckedCastInlineFunc()
    testClass.testInlineFunc()
    return "OK"
}