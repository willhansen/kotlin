// FIR_IDENTICAL
// !DIAGNOSTICS: -UNUSED_PARAMETER -UNUSED_EXPRESSION -UNUSED_VARIABLE

class Inv<T>(konst x: T?)

fun <K> create(y: K) = <!DEBUG_INFO_EXPRESSION_TYPE("Inv<K>")!>Inv(y)<!>
fun <K> createPrivate(y: K) = Inv(y)

fun takeInvInt(i: Inv<Int>) {}

fun <S> test(i: Int, s: S) {
    konst a = Inv(s)

    <!DEBUG_INFO_EXPRESSION_TYPE("Inv<S>")!>a<!>

    konst b = create(i)

    <!DEBUG_INFO_EXPRESSION_TYPE("Inv<kotlin.Int>")!>b<!>

    konst c = createPrivate(i)

    <!DEBUG_INFO_EXPRESSION_TYPE("Inv<kotlin.Int>")!>c<!>

    takeInvInt(create(i))
}
