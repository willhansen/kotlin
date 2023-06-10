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
    test1

    konst test2 = if (bool) {
        id { "2" }
    } else null
    test2

    konst test3 = if (bool) {
        Inv { "3" }
    } else null
    test3

    konst test4 = if (bool) {
        4 to { "4" }
    } else null
    test4

    konst test5 = if (bool) {
        {{ "5" }}
    } else null
    test5

    konst test6 = if (bool) {
        id { { "6" } }
    } else null
    test6

    konst test7 = if (bool) {
        Inv { { "7" } }
    } else null
    test7

    konst test8 = if (bool) {
        8 to { { "8" } }
    } else null
    test8

    konst test9 = select({ "9" }, null)
    test9

    konst test10 = select(id { "10" }, null)
    test10

    konst test11 = select(null, Inv { "11" })
    test11

    konst test12 = select({ 12 to "" }, null)
    test12

    konst test13: Pair<Int, () -> () -> String>? = if(bool) {
        13 to { { "13" } }
    } else null
    test13
}