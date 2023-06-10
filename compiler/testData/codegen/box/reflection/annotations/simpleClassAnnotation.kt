// TARGET_BACKEND: JVM
// WITH_REFLECT

@Retention(AnnotationRetention.RUNTIME)
annotation class Simple(konst konstue: String)

@Simple("OK")
class A

fun box(): String {
    return (A::class.annotations.single() as Simple).konstue
}
