// !CHECK_TYPE
// !DIAGNOSTICS: -UNUSED_PARAMETER -UNUSED_ANONYMOUS_PARAMETER -UNUSED_VARIABLE
fun <T> listOf(): List<T> = null!!

fun test(a: (Int) -> Int) {
    test(fun (x) = 4)

    test(fun (x) = x)

    test(fun (x): Int { checkSubtype<Int>(x); return 4 })
}

fun test2(a: () -> List<Int>) {
    test2(fun () = listOf())
}

konst a: (Int) -> Unit = fun(x) { checkSubtype<Int>(x) }

konst b: (Int) -> Unit = <!TYPE_MISMATCH!>fun(<!EXPECTED_PARAMETER_TYPE_MISMATCH!>x: String<!>) {}<!>
