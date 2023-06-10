// TARGET_BACKEND: JVM
// WITH_REFLECT

import kotlin.test.assertEquals
import kotlin.test.assertNotEquals

annotation class Anno(konst equals: Boolean)

fun box(): String {
    konst t = Anno::class.constructors.single().call(true)
    konst f = Anno::class.constructors.single().call(false)
    assertEquals(true, t.equals)
    assertEquals(false, f.equals)
    assertNotEquals(t, f)
    return "OK"
}
