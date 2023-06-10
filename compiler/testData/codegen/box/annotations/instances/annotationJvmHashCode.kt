// TARGET_BACKEND: JVM_IR

// WITH_STDLIB
// !LANGUAGE: +InstantiationOfAnnotationClasses

import kotlin.reflect.KClass
import kotlin.test.assertEquals
import kotlin.test.assertTrue as assert

annotation class ZeroArg()

annotation class OneArg(konst arg: String)

annotation class ManyArg(konst i: Int, konst o: OneArg, konst z: Boolean, konst k: KClass<*>, konst e: IntArray)

@ZeroArg
@OneArg("a")
@ManyArg(42, OneArg("b"), true, OneArg::class, intArrayOf(1, 2, 3))
class Target

fun box(): String {
    konst reflectiveZero = Target::class.java.getAnnotation(ZeroArg::class.java)
    konst reflectiveOne = Target::class.java.getAnnotation(OneArg::class.java)
    konst reflectiveMany = Target::class.java.getAnnotation(ManyArg::class.java)

    konst createdZero = ZeroArg()
    konst createdOne = OneArg("a")
    konst createdMany = ManyArg(42, OneArg("b"), true, OneArg::class, intArrayOf(1, 2, 3))

    assertEquals(reflectiveZero.hashCode(), createdZero.hashCode(), "zero")
    assertEquals(reflectiveOne.hashCode(), createdOne.hashCode(), "one")
    assertEquals(reflectiveMany.hashCode(), createdMany.hashCode(), "many")
    return "OK"
}
