// TARGET_BACKEND: JVM
// WITH_REFLECT

package test

import kotlin.reflect.KClass
import kotlin.test.assertEquals

@Target(AnnotationTarget.TYPE)
annotation class Anno(
    konst k1: KClass<out CharSequence>,
    konst k2: KClass<in String>,
    konst ka: Array<KClass<out Number>>
)

fun f(): @Anno(String::class, CharSequence::class, [Double::class, Long::class, Int::class]) Unit {}

fun box(): String {
    assertEquals(
        "[@test.Anno(k1=class java.lang.String, k2=interface java.lang.CharSequence, " +
                "ka=[class java.lang.Double, class java.lang.Long, class java.lang.Integer])]",
        ::f.returnType.annotations.toString()
    )

    return "OK"
}
