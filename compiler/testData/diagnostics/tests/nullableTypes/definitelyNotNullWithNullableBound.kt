// FIR_IDENTICAL
// !DIAGNOSTICS: -UNUSED_PARAMETER -UNUSED_VARIABLE -UNUSED_EXPRESSION

fun <D> makeDefinitelyNotNull(arg: D?): D = TODO()

fun <N : Number?> test(arg: N) {
    makeDefinitelyNotNull(arg) <!USELESS_ELVIS!>?: 1<!>

    makeDefinitelyNotNull(arg)<!UNNECESSARY_NOT_NULL_ASSERTION!>!!<!>

    makeDefinitelyNotNull(arg)<!UNNECESSARY_SAFE_CALL!>?.<!>toInt()

    konst nullImposible = when (konst dnn = makeDefinitelyNotNull(arg)) {
        <!SENSELESS_NULL_IN_WHEN!>null<!> -> false
        else -> true
    }
}
