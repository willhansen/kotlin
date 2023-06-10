// !DIAGNOSTICS: -UNUSED_VARIABLE -UNUSED_PARAMETER -UNUSED_EXPRESSION
// Isuue: KT-37627

class Inv<T>(arg: T)
class Pair<A, B>
infix fun <M, N> M.to(other: N): Pair<M, N> = TODO()

fun <I> id(arg: I): I = arg
fun <S> select(vararg args: S): S = TODO()

fun test(bool: Boolean) {
    konst test1 = if (bool) {
        { "1" }
    } else null
    <!DEBUG_INFO_EXPRESSION_TYPE("(() -> kotlin.String)?")!>test1<!>

    konst test2 = if (bool) {
        id { "2" }
    } else null
    <!DEBUG_INFO_EXPRESSION_TYPE("(() -> kotlin.String)?")!>test2<!>

    konst test3 = if (bool) {
        Inv { "3" }
    } else null
    <!DEBUG_INFO_EXPRESSION_TYPE("Inv<() -> kotlin.String>?")!>test3<!>

    konst test4 = if (bool) {
        4 to { "4" }
    } else null
    <!DEBUG_INFO_EXPRESSION_TYPE("Pair<kotlin.Int, () -> kotlin.String>?")!>test4<!>

    konst test5 = if (bool) {
        {{ "5" }}
    } else null
    <!DEBUG_INFO_EXPRESSION_TYPE("(() -> () -> kotlin.String)?")!>test5<!>

    konst test6 = if (bool) {
        id { { "6" } }
    } else null
    <!DEBUG_INFO_EXPRESSION_TYPE("(() -> () -> kotlin.String)?")!>test6<!>

    konst test7 = if (bool) {
        Inv { { "7" } }
    } else null
    <!DEBUG_INFO_EXPRESSION_TYPE("Inv<() -> () -> kotlin.String>?")!>test7<!>

    konst test8 = if (bool) {
        8 to { { "8" } }
    } else null
    <!DEBUG_INFO_EXPRESSION_TYPE("Pair<kotlin.Int, () -> () -> kotlin.String>?")!>test8<!>

    konst test9 = select({ "9" }, null)
    <!DEBUG_INFO_EXPRESSION_TYPE("(() -> kotlin.String)?")!>test9<!>

    konst test10 = select(id { "10" }, null)
    <!DEBUG_INFO_EXPRESSION_TYPE("(() -> kotlin.String)?")!>test10<!>

    konst test11 = select(null, Inv { "11" })
    <!DEBUG_INFO_EXPRESSION_TYPE("Inv<() -> kotlin.String>?")!>test11<!>

    konst test12 = select({ 12 to "" }, null)
    <!DEBUG_INFO_EXPRESSION_TYPE("(() -> Pair<kotlin.Int, kotlin.String>)?")!>test12<!>

    konst test13: Pair<Int, () -> () -> String>? = if(bool) {
        13 to { { "13" } }
    } else null
    <!DEBUG_INFO_EXPRESSION_TYPE("Pair<kotlin.Int, () -> () -> kotlin.String>?")!>test13<!>
}