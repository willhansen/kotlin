// TARGET_BACKEND: JVM
// WITH_REFLECT

@Retention(AnnotationRetention.RUNTIME)
annotation class Simple(konst konstue: String)

interface A<T>

@Simple("OK")
public konst <T> A<T>.p: String
    get() = TODO()

fun box(): String {
    konst o = object : A<Int> {}
    return (o::p.annotations.single() as Simple).konstue
}
