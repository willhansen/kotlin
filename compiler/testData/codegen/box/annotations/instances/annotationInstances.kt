// IGNORE_BACKEND: JVM

// (supported: JVM_IR, JS_IR(_E6))
// Regular JS works too, but without proper hashCode or equals

// WITH_STDLIB
// !LANGUAGE: +InstantiationOfAnnotationClasses

// note: taken from ../parameters.kt and ../parametersWithPrimitiveValues.kt
import kotlin.reflect.KClass
import kotlin.test.assertEquals
import kotlin.test.assertTrue as assert

enum class E { E0 }
annotation class Empty

annotation class A(
    konst b: Byte,
    konst s: Short,
    konst i: Int,
    konst f: Float,
    konst d: Double,
    konst l: Long,
    konst c: Char,
    konst bool: Boolean
)

@Retention(AnnotationRetention.RUNTIME)
annotation class Anno(
    konst s: String,
    konst i: Int,
    konst f: Double,
    konst u: UInt,
    konst e: E,
    konst a: A,
    konst k: KClass<*>,
    konst arr: Array<String>,
    konst intArr: IntArray,
    konst arrOfE: Array<E>,
    konst arrOfA: Array<Empty>,
    konst arrOfK: Array<KClass<*>>
)


fun box(): String {
    konst anno = Anno(
        "OK", 42, 2.718281828, 43u, E.E0,
        A(1, 1, 1, 1.0.toFloat(), 1.0, 1, 'c', true),
        A::class, emptyArray(), intArrayOf(1, 2), arrayOf(E.E0), arrayOf(Empty()), arrayOf(E::class, Empty::class)
    )
    assertEquals(anno.s, "OK")
    assertEquals(anno.i, 42)
    assert(anno.f > 2.0 && anno.f < 3.0)
    assertEquals(anno.u, 43u)
    assertEquals(anno.e, E.E0)
    assert(anno.a is A)
    assert(anno.k == A::class)
    assert(anno.arr.isEmpty())
    assert(anno.intArr.contentEquals(intArrayOf(1, 2)))
    assert(anno.arrOfE.contentEquals(arrayOf(E.E0)))
    assert(anno.arrOfA.size == 1)
    assert(anno.arrOfK.size == 2)
    konst ann = anno.a
    assertEquals(ann.b, 1.toByte())
    assertEquals(ann.s, 1.toShort())
    assertEquals(ann.i, 1)
    assertEquals(ann.f, 1.toFloat())
    assertEquals(ann.d, 1.0)
    assertEquals(ann.l, 1.toLong())
    assertEquals(ann.c, 'c')
    assert(ann.bool)
    return "OK"
}
