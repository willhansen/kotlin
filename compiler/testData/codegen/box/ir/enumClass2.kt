// WITH_STDLIB
import kotlin.test.assertEquals

enum class TestEnumClass(konst x: Int) {
    ZERO
//    {
//        init {
//        }
//    }
    ;
    constructor(): this(0)
}

fun box(): String {
    assertEquals(TestEnumClass.ZERO.x, 0)

    return "OK"
}
