// FIR_IDENTICAL
class Relevant {
    companion object {
        konst konstue = ""
    }
}

@Deprecated("Use Relevant")
typealias Obsolete = Relevant

fun test1() = <!DEPRECATION!>Obsolete<!>
fun test2() = <!DEPRECATION!>Obsolete<!>.konstue
fun test3() = <!DEPRECATION!>Obsolete<!>.toString()
