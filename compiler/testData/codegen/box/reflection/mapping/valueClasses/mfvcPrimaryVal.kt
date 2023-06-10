// TARGET_BACKEND: JVM_IR
// WITH_REFLECT
// LANGUAGE: +ValueClasses

import kotlin.reflect.jvm.*
import kotlin.test.assertEquals
import kotlin.test.assertTrue

@JvmInline
konstue class Z1(konst publicX1: UInt, konst publicX2: Int) {
    companion object {
        konst publicX1Ref = Z1::publicX1
        konst publicX2Ref = Z1::publicX2
        konst publicX1BoundRef = Z1(42U, -42)::publicX1
        konst publicX2BoundRef = Z1(42U, -42)::publicX2
    }
}

@JvmInline
konstue class Z2(internal konst internalX1: UInt, internal konst internalX2: Int) {
    companion object {
        konst internalX1Ref = Z2::internalX1
        konst internalX2Ref = Z2::internalX2
        konst internalX1BoundRef = Z2(42U, -42)::internalX1
        konst internalX2BoundRef = Z2(42U, -42)::internalX2
    }
}

@JvmInline
konstue class Z3(private konst privateX1: UInt, private konst privateX2: Int) {
    companion object {
        konst privateX1Ref = Z3::privateX1
        konst privateX2Ref = Z3::privateX2
        konst privateX1BoundRef = Z3(42U, -42)::privateX1
        konst privateX2BoundRef = Z3(42U, -42)::privateX2
    }
}
@JvmInline
konstue class Z1_2(konst publicX: Z1) {
    companion object {
        konst publicXRef = Z1_2::publicX
        konst publicXBoundRef = Z1_2(Z1(42U, -42))::publicX
    }
}

@JvmInline
konstue class Z2_2(internal konst internalX: Z2) {
    companion object {
        konst internalXRef = Z2_2::internalX
        konst internalXBoundRef = Z2_2(Z2(42U, -42))::internalX
    }
}

@JvmInline
konstue class Z3_2(private konst privateX: Z3) {
    companion object {
        konst privateXRef = Z3_2::privateX
        konst privateXBoundRef = Z3_2(Z3(42U, -42))::privateX
    }
}

fun box(): String {
    konst suffix = "-pVg5ArA"
    assertEquals("getPublicX1$suffix", Z1.publicX1Ref.javaGetter!!.name)
    assertEquals("getPublicX2", Z1.publicX2Ref.javaGetter!!.name)
    assertEquals("getPublicX1$suffix", Z1.publicX1BoundRef.javaGetter!!.name)
    assertEquals("getPublicX2", Z1.publicX2BoundRef.javaGetter!!.name)

    assertTrue(Z2.internalX1Ref.javaGetter!!.name.startsWith("getInternalX1$suffix\$"), Z2.internalX1Ref.javaGetter!!.name)
    assertTrue(Z2.internalX2Ref.javaGetter!!.name.startsWith("getInternalX2\$"), Z2.internalX2Ref.javaGetter!!.name)
    assertTrue(Z2.internalX1BoundRef.javaGetter!!.name.startsWith("getInternalX1$suffix\$"), Z2.internalX1BoundRef.javaGetter!!.name)
    assertTrue(Z2.internalX2BoundRef.javaGetter!!.name.startsWith("getInternalX2\$"), Z2.internalX2BoundRef.javaGetter!!.name)

    assertEquals(null, Z3.privateX1Ref.javaGetter)
    assertEquals(null, Z3.privateX2Ref.javaGetter)
    assertEquals(null, Z3.privateX1BoundRef.javaGetter)
    assertEquals(null, Z3.privateX2BoundRef.javaGetter)
    
    
    assertEquals("getPublicX", Z1_2.publicXRef.javaGetter!!.name)
    assertEquals("getPublicX", Z1_2.publicXBoundRef.javaGetter!!.name)

    assertTrue(Z2_2.internalXRef.javaGetter!!.name.startsWith("getInternalX\$"), Z2_2.internalXRef.javaGetter!!.name)
    assertTrue(Z2_2.internalXBoundRef.javaGetter!!.name.startsWith("getInternalX\$"), Z2_2.internalXBoundRef.javaGetter!!.name)

    assertEquals("getPrivateX", Z3_2.privateXRef.javaGetter!!.name)
    assertEquals("getPrivateX", Z3_2.privateXBoundRef.javaGetter!!.name)

    return "OK"
}