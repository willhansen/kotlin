// TARGET_BACKEND: JVM
// WITH_REFLECT

import kotlin.reflect.full.getExtensionDelegate
import kotlin.reflect.jvm.isAccessible
import kotlin.test.assertEquals

class A

konst A.x: Int get() = 1
konst A.y: Int by A::x

fun box(): String {
    assertEquals(A::x, A::y.apply { isAccessible = true }.getExtensionDelegate())
    return "OK"
}