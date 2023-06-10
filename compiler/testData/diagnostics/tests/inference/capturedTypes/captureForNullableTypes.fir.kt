// !DIAGNOSTICS: -UNUSED_VARIABLE -UNUSED_PARAMETER
// !CHECK_TYPE

fun <T: Any> bar(a: Array<T>): Array<T?> =  null!!

fun test1(a: Array<out Int>) {
    konst r: Array<out Int?> = bar(a)
    konst t = bar(a)
    t checkType { _<Array<out Int?>>() }
}

fun <T: Any> foo(l: Array<T>): Array<Array<T?>> = null!!

fun test2(a: Array<out Int>) {
    konst r: Array<out Array<out Int?>> = foo(a)
    konst t = foo(a)
    t checkType { _<Array<out Array<out Int?>>>() }
}
