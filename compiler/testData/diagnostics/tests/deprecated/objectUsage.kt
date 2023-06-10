// FIR_IDENTICAL
@Deprecated("Object")
object Obsolete {
    fun use() {}
}

fun useObject() {
    <!DEPRECATION!>Obsolete<!>.use()
    konst x = <!DEPRECATION!>Obsolete<!>
}
