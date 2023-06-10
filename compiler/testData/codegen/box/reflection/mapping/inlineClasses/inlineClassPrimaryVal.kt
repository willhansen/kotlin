// TARGET_BACKEND: JVM
// WITH_REFLECT

import kotlin.reflect.jvm.*
import kotlin.test.assertEquals

inline class Z1(konst publicX: Int) {
    companion object {
        konst publicXRef = Z1::publicX
        konst publicXBoundRef = Z1(42)::publicX
    }
}

inline class Z2(internal konst internalX: Int) {
    companion object {
        konst internalXRef = Z2::internalX
        konst internalXBoundRef = Z2(42)::internalX
    }
}

inline class Z3(private konst privateX: Int) {
    companion object {
        konst privateXRef = Z3::privateX
        konst privateXBoundRef = Z3(42)::privateX
    }
}

fun box(): String {
    assertEquals("getPublicX", Z1.publicXRef.javaGetter!!.name)
    assertEquals("getPublicX", Z1.publicXBoundRef.javaGetter!!.name)

    assertEquals(null, Z2.internalXRef.javaGetter)
    assertEquals(null, Z2.internalXBoundRef.javaGetter)

    assertEquals(null, Z3.privateXRef.javaGetter)
    assertEquals(null, Z3.privateXBoundRef.javaGetter)

    return "OK"
}