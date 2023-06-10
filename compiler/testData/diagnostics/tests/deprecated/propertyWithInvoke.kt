@Deprecated("No")
konst f: () -> Unit = {}

fun test() {
    <!DEPRECATION!>f<!>()
}
