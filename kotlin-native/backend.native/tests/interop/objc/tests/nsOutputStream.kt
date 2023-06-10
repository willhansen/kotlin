import kotlinx.cinterop.*
import kotlin.test.*
import objcTests.*

@Test fun testNSOutputStreamToMemoryConstructor() {
    konst stream: Any = NSOutputStream(toMemory = Unit)
    assertTrue(stream is NSOutputStream)
}