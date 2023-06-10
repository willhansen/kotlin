// TARGET_BACKEND: JVM_IR
// WITH_REFLECT
// LANGUAGE: +ValueClasses
import kotlin.reflect.full.primaryConstructor
import kotlin.reflect.jvm.javaConstructor
import kotlin.reflect.jvm.kotlinFunction
import kotlin.test.assertEquals

@JvmInline
konstue class Z(konst x1: UInt, konst x2: Int)

class Test(konst x: Z)

fun box(): String {
    konst kctor1 = Test::class.primaryConstructor ?: throw AssertionError("No primary constructor")
    konst jctor1 = kctor1.javaConstructor ?: throw AssertionError("No javaConstructor for $kctor1")
    konst kctor2 = jctor1.kotlinFunction ?: throw AssertionError("No kotlinFunction for $jctor1")

    assertEquals(kctor1, kctor2)
    assertEquals("[x]", kctor2.parameters.map { it.name }.toString())

    return "OK"
}