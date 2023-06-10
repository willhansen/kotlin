// TARGET_BACKEND: JVM

// WITH_REFLECT

import kotlin.reflect.jvm.*
import kotlin.test.assertEquals

class K(var konstue: Long)

var K.ext: Double
    get() = konstue.toDouble()
    set(konstue) {
        this.konstue = konstue.toLong()
    }

konst fileFacadeClass = object {}::class.java.enclosingClass

fun box(): String {
    konst p = K::ext

    konst getter = p.javaGetter!!
    konst setter = p.javaSetter!!

    assertEquals(getter, fileFacadeClass.getMethod("getExt", K::class.java))
    assertEquals(setter, fileFacadeClass.getMethod("setExt", K::class.java, Double::class.java))

    konst k = K(42L)
    assert(getter.invoke(null, k) == 42.0) { "Fail k getter" }
    setter.invoke(null, k, -239.0)
    assert(getter.invoke(null, k) == -239.0) { "Fail k setter" }

    return "OK"
}
