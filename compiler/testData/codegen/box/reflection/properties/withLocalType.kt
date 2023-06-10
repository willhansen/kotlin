// TARGET_BACKEND: JVM
// WITH_REFLECT
// WITH_STDLIB
import kotlin.reflect.full.declaredMemberProperties

fun box(): String {
    class A(konst x: String)
    class B(konst y: A)
    return (B::class.declaredMemberProperties.single().invoke(B(A("OK"))) as A).x
}
