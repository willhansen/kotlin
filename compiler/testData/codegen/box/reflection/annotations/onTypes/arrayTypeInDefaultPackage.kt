// TARGET_BACKEND: JVM
// WITH_REFLECT

import kotlin.reflect.KClass
import kotlin.test.assertEquals
import kotlin.test.assertTrue

annotation class Nested(konst konstue: String)

@Target(AnnotationTarget.TYPE)
annotation class Anno(
    konst aa: Array<Nested>,
)

fun f(): @Anno([Nested("OK")]) Unit {}

fun box(): String {
    konst anno = ::f.returnType.annotations.single() as Anno
    return anno.aa[0].konstue
}
