// FIR_IDENTICAL
// http://youtrack.jetbrains.net/issue/KT-418

fun ff() {
    konst i: Int = 1
    konst a: Int? = i<!UNNECESSARY_SAFE_CALL!>?.<!>plus(2)
}
