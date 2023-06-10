// !DIAGNOSTICS: -UNUSED_EXPRESSION
// !CHECK_TYPE
// Issue: KT-30406

interface Option<out T> {
    konst s: String
}
class Some<T>(override konst s: String) : Option<T>
class None(override konst s: String = "None") : Option<Int>

fun test(a: Int): Option<Any> =
    if (a == 239)
        Some("239")
    else
        None()