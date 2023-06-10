// TARGET_BACKEND: JVM
// WITH_REFLECT

@Retention(AnnotationRetention.RUNTIME)
annotation class Simple(konst konstue: String)

@Simple("OK")
fun box(): String {
    return (::box.annotations.single() as Simple).konstue
}
