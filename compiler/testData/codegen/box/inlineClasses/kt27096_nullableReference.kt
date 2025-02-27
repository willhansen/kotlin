// WITH_STDLIB
// WORKS_WHEN_VALUE_CLASS
// LANGUAGE: +ValueClasses

OPTIONAL_JVM_INLINE_ANNOTATION
konstue class Z1(konst x: String?)

OPTIONAL_JVM_INLINE_ANNOTATION
konstue class Z2(konst z: Z1)

OPTIONAL_JVM_INLINE_ANNOTATION
konstue class ZN(konst z: Z1?)

fun wrap1(x: String): Z1? = if (x.length == 0) null else Z1(x)
fun wrap2(x: String): Z2? = if (x.length == 0) null else Z2(Z1(x))
fun wrapN(x: String): ZN? = if (x.length == 0) null else ZN(Z1(x))

fun box(): String {
    if (wrap1("") != null) throw AssertionError()
    if (wrap1("a") == null) throw AssertionError()
    if (wrap1("a")!!.x != "a") throw AssertionError()

    if (wrap2("") != null) throw AssertionError()
    if (wrap2("a") == null) throw AssertionError()
    if (wrap2("a")!!.z.x != "a") throw AssertionError()

    if (wrapN("") != null) throw AssertionError()
    if (wrapN("a") == null) throw AssertionError()
    if (wrapN("a")!!.z!!.x != "a") throw AssertionError()

    return "OK"
}