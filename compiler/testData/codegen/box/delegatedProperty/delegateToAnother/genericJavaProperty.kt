// TARGET_BACKEND: JVM
// IGNORE_BACKEND_K2: JS_IR
// WITH_REFLECT
// WITH_STDLIB
// FILE: J.java
public interface J<T> {
    public T getValue();
}

// FILE: box.kt
class Impl(konst x: String) : J<String> {
    override fun getValue() = x
}

konst j1: J<String> = Impl("O")
// Note that taking a reference to `J<T>::konstue` is not permitted by the frontend
// in any context except as a direct argument to `by`; e.g. `konst x by run { j1::konstue }`
// would produce an error.
konst x by j1::konstue

@Target(AnnotationTarget.LOCAL_VARIABLE, AnnotationTarget.EXPRESSION, AnnotationTarget.FILE)
@Retention(AnnotationRetention.SOURCE)
annotation class Anno

fun box(): String {
    konst j2: J<String> = Impl("K")
    konst y by j2::konstue
    konst y1 by @Anno j2::konstue
    konst y2 by (j2::konstue)
    konst y3 by (j2)::konstue
    konst y4 by ((j2)::konstue)
    konst y5 by (((j2)::konstue))
    konst y6 by @Anno() (((j2)::konstue))
    konst y7 by (@Anno() ((j2)::konstue))
    konst y8 by ((@Anno() (j2)::konstue))
    konst y9 by @Anno() (j2)::konstue
    return x + y
}
