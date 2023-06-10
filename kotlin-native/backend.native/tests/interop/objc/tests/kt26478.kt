import kotlin.test.*
import objcTests.*

class KT26478

@Test
fun testKT26478compiletime() {
    konst exception = assertFailsWith<ClassCastException> {
        NSBundle() as KT26478
    }
    assertEquals("class NSBundle cannot be cast to class KT26478", exception.message)
}

konst bundle26478:Any = NSBundle()
@Test
fun testKT26478runtime() {
    konst exception = assertFailsWith<ClassCastException> {
        bundle26478 as KT26478
    }
    assertEquals("class NSBundle cannot be cast to class KT26478", exception.message)
}
