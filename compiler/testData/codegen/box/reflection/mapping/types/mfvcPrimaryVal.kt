// TARGET_BACKEND: JVM_IR
// WITH_REFLECT
// LANGUAGE: +ValueClasses

package test

import kotlin.reflect.KCallable
import kotlin.reflect.jvm.*
import kotlin.test.assertEquals

@JvmInline
konstue class Z1(konst publicX1: UInt, konst publicX2: Int) {
    companion object {
        konst publicX1Ref = Z1::publicX1
        konst publicX2Ref = Z1::publicX2
        konst publicX1BoundRef = Z1(42U, 43)::publicX1
        konst publicX2BoundRef = Z1(42U, 43)::publicX2
    }
}

@JvmInline
konstue class Z2(internal konst internalX1: UInt, internal konst internalX2: Int) {
    companion object {
        konst internalX1Ref = Z2::internalX1
        konst internalX2Ref = Z2::internalX2
        konst internalX1BoundRef = Z2(42U, 43)::internalX1
        konst internalX2BoundRef = Z2(42U, 43)::internalX2
    }
}

@JvmInline
konstue class Z3(private konst privateX1: UInt, private konst privateX2: Int) {
    companion object {
        konst privateX1Ref = Z3::privateX1
        konst privateX2Ref = Z3::privateX2
        konst privateX1BoundRef = Z3(42U, 43)::privateX1
        konst privateX2BoundRef = Z3(42U, 43)::privateX2
    }
}

@JvmInline
konstue class ZZ(konst x1: Z1, konst x2: Z1)

fun KCallable<*>.getJavaTypesOfParams() = parameters.map { it.type.javaType }.toString()
fun KCallable<*>.getJavaTypeOfResult() = returnType.javaType.toString()

fun box(): String {
    assertEquals("[class test.Z1]", Z1.publicX1Ref.getJavaTypesOfParams())
    assertEquals("[class test.Z1]", Z1.publicX2Ref.getJavaTypesOfParams())
    assertEquals("int", Z1.publicX1Ref.getJavaTypeOfResult())
    assertEquals("int", Z1.publicX2Ref.getJavaTypeOfResult())

    assertEquals("[]", Z1.publicX1BoundRef.getJavaTypesOfParams())
    assertEquals("[]", Z1.publicX2BoundRef.getJavaTypesOfParams())
    assertEquals("int", Z1.publicX1BoundRef.getJavaTypeOfResult())
    assertEquals("int", Z1.publicX2BoundRef.getJavaTypeOfResult())

    assertEquals("[class test.Z2]", Z2.internalX1Ref.getJavaTypesOfParams())
    assertEquals("[class test.Z2]", Z2.internalX2Ref.getJavaTypesOfParams())
    assertEquals("int", Z2.internalX1Ref.getJavaTypeOfResult())
    assertEquals("int", Z2.internalX2Ref.getJavaTypeOfResult())

    assertEquals("[]", Z2.internalX1BoundRef.getJavaTypesOfParams())
    assertEquals("[]", Z2.internalX2BoundRef.getJavaTypesOfParams())
    assertEquals("int", Z2.internalX1BoundRef.getJavaTypeOfResult())
    assertEquals("int", Z2.internalX2BoundRef.getJavaTypeOfResult())

    assertEquals("[class test.Z3]", Z3.privateX1Ref.getJavaTypesOfParams())
    assertEquals("[class test.Z3]", Z3.privateX2Ref.getJavaTypesOfParams())
    assertEquals("int", Z3.privateX1Ref.getJavaTypeOfResult())
    assertEquals("int", Z3.privateX2Ref.getJavaTypeOfResult())

    assertEquals("[]", Z3.privateX1BoundRef.getJavaTypesOfParams())
    assertEquals("[]", Z3.privateX2BoundRef.getJavaTypesOfParams())
    assertEquals("int", Z3.privateX1BoundRef.getJavaTypeOfResult())
    assertEquals("int", Z3.privateX2BoundRef.getJavaTypeOfResult())


    assertEquals("[class test.ZZ]", ZZ::x1.getJavaTypesOfParams())
    assertEquals("[class test.ZZ]", ZZ::x2.getJavaTypesOfParams())
    assertEquals("class test.Z1", ZZ::x1.getJavaTypeOfResult())
    assertEquals("class test.Z1", ZZ::x2.getJavaTypeOfResult())

    return "OK"
}
