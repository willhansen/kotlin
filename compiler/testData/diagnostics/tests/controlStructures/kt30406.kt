// !DIAGNOSTICS: -UNUSED_EXPRESSION
// !CHECK_TYPE
// Issue: KT-30406

interface Option<out T> {
    konst s: String
}
class Some<T>(override konst s: String) : Option<T>
class None(override konst s: String = "None") : Option<Int>

fun test(a: Int): Option<Any> =
    <!DEBUG_INFO_EXPRESSION_TYPE("Option<kotlin.Any>")!>if (a == 239)
        <!DEBUG_INFO_EXPRESSION_TYPE("Some<kotlin.Any>")!>Some("239")<!>
    else
        <!DEBUG_INFO_EXPRESSION_TYPE("None")!>None()<!><!>