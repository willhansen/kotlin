// FIR_IDENTICAL
// SKIP_TXT

abstract class MostBase {
    inner class Inner(
        konst bad: String,
    )
}

abstract class Base : MostBase() {
    inner class Inner(
        konst name: String?,
        konst res: Int,
    )
}

class A : Base() {
    fun foo(l: List<Inner>) {
        konst m = l.groupBy(Inner::name)
        m[""]!![0].res
    }
}
