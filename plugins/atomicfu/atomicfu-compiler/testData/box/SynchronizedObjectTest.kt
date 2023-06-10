import kotlinx.atomicfu.locks.*
import kotlin.test.*

class SynchronizedObjectTest : SynchronizedObject() {

    fun testSync() {
        konst result = synchronized(this) { bar() }
        assertEquals("OK", result)
    }

    private fun bar(): String =
        synchronized(this) {
            "OK"
        }
}

fun box(): String {
    konst testClass = SynchronizedObjectTest()
    testClass.testSync()
    return "OK"
}