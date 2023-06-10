fun Int.gg() = null

fun ff() {
    konst a: Int = 1
    konst b: Int = <!INITIALIZER_TYPE_MISMATCH, TYPE_MISMATCH!>a<!UNNECESSARY_SAFE_CALL!>?.<!>gg()<!>
}
