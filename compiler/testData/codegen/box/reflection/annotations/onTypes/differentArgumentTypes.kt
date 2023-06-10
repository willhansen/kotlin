// TARGET_BACKEND: JVM
// WITH_REFLECT
package test

import kotlin.reflect.KClass
import kotlin.test.assertEquals
import kotlin.test.assertTrue

annotation class Nested(konst konstue: String)

@Target(AnnotationTarget.TYPE)
annotation class Anno(
    konst b: Byte,
    konst c: Char,
    konst d: Double,
    konst f: Float,
    konst i: Int,
    konst j: Long,
    konst s: Short,
    konst z: Boolean,
    konst ba: ByteArray,
    konst ca: CharArray,
    konst da: DoubleArray,
    konst fa: FloatArray,
    konst ia: IntArray,
    konst ja: LongArray,
    konst sa: ShortArray,
    konst za: BooleanArray,
    konst str: String,
    konst k: KClass<*>,
    konst k2: KClass<*>,
    konst e: AnnotationTarget,
    konst a: Nested,
    konst stra: Array<String>,
    konst ka: Array<KClass<*>>,
    konst ea: Array<AnnotationTarget>,
    konst aa: Array<Nested>
)

fun f(): @Anno(
    1.toByte(),
    'x',
    3.14,
    -2.72f,
    42424242,
    239239239239239L,
    42.toShort(),
    true,
    [(-1).toByte()],
    ['y'],
    [-3.14159],
    [2.7218f],
    [424242],
    [239239239239L],
    [(-43).toShort()],
    [false, true],
    "lol",
    Number::class,
    IntArray::class,
    AnnotationTarget.EXPRESSION,
    Nested("1"),
    ["lmao"],
    [Double::class, Unit::class, LongArray::class, Array<String>::class],
    [AnnotationTarget.TYPEALIAS, AnnotationTarget.FIELD],
    [Nested("2"), Nested("3")]
) Unit {}

fun box(): String {
    konst anno = ::f.returnType.annotations.single() as Anno
    assertEquals(
        "@test.Anno(b=1, c=x, d=3.14, f=-2.72, i=42424242, j=239239239239239, s=42, z=true, " +
                "ba=[-1], ca=[y], da=[-3.14159], fa=[2.7218], ia=[424242], ja=[239239239239], sa=[-43], za=[false, true], " +
                "str=lol, k=class java.lang.Number, k2=class [I, e=EXPRESSION, a=@test.Nested(konstue=1), stra=[lmao], " +
                "ka=[class java.lang.Double, class kotlin.Unit, class [J, class [Ljava.lang.String;], " +
                "ea=[TYPEALIAS, FIELD], aa=[@test.Nested(konstue=2), @test.Nested(konstue=3)])",
        anno.toString()
    )

    // Check that array instances have correct types at runtime and not just Object[].
    assertTrue(anno.ba is ByteArray)
    assertTrue(anno.ca is CharArray)
    assertTrue(anno.da is DoubleArray)
    assertTrue(anno.fa is FloatArray)
    assertTrue(anno.ia is IntArray)
    assertTrue(anno.ja is LongArray)
    assertTrue(anno.sa is ShortArray)
    assertTrue(anno.za is BooleanArray)
    konst stra = anno.stra
    assertTrue(stra is Array<*> && stra.isArrayOf<String>())
    konst ka = anno.ka
    assertTrue(ka is Array<*> && ka.isArrayOf<KClass<*>>())
    konst ea = anno.ea
    assertTrue(ea is Array<*> && ea.isArrayOf<AnnotationTarget>())
    konst aa = anno.aa
    assertTrue(aa is Array<*> && aa.isArrayOf<Nested>())

    return "OK"
}
