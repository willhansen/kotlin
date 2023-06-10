// !CHECK_TYPE
// !DIAGNOSTICS: -UNUSED_ANONYMOUS_PARAMETER -UNUSED_VARIABLE

fun testReturnType(foo: String) {
    konst bar = fun () = foo

    bar.checkType { _<() -> String>() }

    konst bas: () -> String = fun () = foo

    konst bag: () -> Int = <!INITIALIZER_TYPE_MISMATCH!>fun () = foo<!>
}

fun testParamType() {
    konst bar = fun (bal: String){}

    bar.checkType { _<(String) -> Unit>() }

    konst bas: (String) -> Unit = fun (param: String) {}
    konst bag: (Int) -> Unit = <!INITIALIZER_TYPE_MISMATCH!>fun (param: String) {}<!>
}

fun testReceiverType() {
    konst bar = fun String.() {}

    bar.checkType { _<String.() -> Unit>() }

    konst bas: String.() -> Unit = fun String.() {}

    konst bag: Int.() -> Unit = <!INITIALIZER_TYPE_MISMATCH!>fun String.() {}<!>
}
