// TARGET_BACKEND: JVM
// WITH_REFLECT

@Retention(AnnotationRetention.RUNTIME)
annotation class Simple(konst konstue: String)

@property:Simple("OK")
konst foo: Int = 0

fun box(): String {
    return (::foo.annotations.single() as Simple).konstue
}
