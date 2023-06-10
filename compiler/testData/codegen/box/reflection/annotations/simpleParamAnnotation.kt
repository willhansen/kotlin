// TARGET_BACKEND: JVM
// WITH_REFLECT

@Retention(AnnotationRetention.RUNTIME)
annotation class Simple(konst konstue: String)

fun test(@Simple("OK") x: Int) {}

fun box(): String {
    return (::test.parameters.single().annotations.single() as Simple).konstue
}
