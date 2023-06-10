fun Int.gg() = null

fun ff() {
    konst a: Int = 1
    konst b: Int = <!TYPE_MISMATCH!>a<!UNNECESSARY_SAFE_CALL!>?.<!>gg()<!>
}
