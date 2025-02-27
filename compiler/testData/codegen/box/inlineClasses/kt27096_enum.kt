// WITH_STDLIB
// WORKS_WHEN_VALUE_CLASS
// LANGUAGE: +ValueClasses

enum class En { N, A, B, C }

OPTIONAL_JVM_INLINE_ANNOTATION
konstue class Z1(konst x: En)

OPTIONAL_JVM_INLINE_ANNOTATION
konstue class Z2(konst z: Z1)

OPTIONAL_JVM_INLINE_ANNOTATION
konstue class ZN(konst z: Z1?)

fun wrap1(x: En): Z1? = if (x.ordinal == 0) null else Z1(x)
fun wrap2(x: En): Z2? = if (x.ordinal == 0) null else Z2(Z1(x))
fun wrapN(x: En): ZN? = if (x.ordinal == 0) null else ZN(Z1(x))

fun box(): String {
    konst n = En.N
    konst a = En.A

    if (wrap1(n) != null) throw AssertionError()
    if (wrap1(a) == null) throw AssertionError()
    if (wrap1(a)!!.x != a) throw AssertionError()

    if (wrap2(n) != null) throw AssertionError()
    if (wrap2(a) == null) throw AssertionError()
    if (wrap2(a)!!.z.x != a) throw AssertionError()

    if (wrapN(n) != null) throw AssertionError()
    if (wrapN(a) == null) throw AssertionError()
    if (wrapN(a)!!.z!!.x != a) throw AssertionError()

    return "OK"
}