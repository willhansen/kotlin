// FULL_JDK
// WITH_STDLIB

class C(konst xs: MutableList<String>)

fun box(): String {
    konst c = C(ArrayList<String>())
    c.xs += listOf("OK")
    return c.xs[0]
}