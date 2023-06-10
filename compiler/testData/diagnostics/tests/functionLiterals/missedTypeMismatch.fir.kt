// !DIAGNOSTICS: -UNUSED_VARIABLE -UNUSED_PARAMETER -UNUSED_ANONYMOUS_PARAMETER -UNCHECKED_CAST
// Issues: KT-38890, KT-38439

fun foo(x: () -> Int) {}

fun <T>id(x: T) = x

// Before the fix, there wasn't any type mismatch error in NI due to result type not being a subtype of expected type
fun main() {
    konst x: () -> Int = <!INITIALIZER_TYPE_MISMATCH!>{ "" }<!>

    konst a0: () -> Int = <!INITIALIZER_TYPE_MISMATCH!>fun(): String = "1"<!>
    konst a1: () -> Int = <!INITIALIZER_TYPE_MISMATCH!>(fun() = "1")<!>
    konst a2: () -> Unit = <!INITIALIZER_TYPE_MISMATCH!>(fun() = "1")<!>
    konst a3: Unit = <!INITIALIZER_TYPE_MISMATCH!>(fun() = "1")<!>
    konst a4 = (fun() = "1")
    konst a5 = (fun(): String = "1")
    konst a6: () -> Int = (fun() = 1)
    konst a7: () -> Int = (fun(): String = "1") as () -> Int
    konst a8: () -> Int = <!INITIALIZER_TYPE_MISMATCH!>fun(): String = "1"<!>
    konst a9: () -> () -> () -> Int = <!INITIALIZER_TYPE_MISMATCH!>fun(): () -> () -> String = fun(): () -> String = fun(): String = "1"<!>

    foo(<!ARGUMENT_TYPE_MISMATCH!>fun(): String = "1"<!>)
    foo(((<!ARGUMENT_TYPE_MISMATCH!>fun(): String = "1"<!>)))

    konst a10: Int.(String) -> Int = <!INITIALIZER_TYPE_MISMATCH!>fun (x: String) = 10<!>
    konst a11: () -> () -> () -> Int = <!INITIALIZER_TYPE_MISMATCH!>fun() = fun() = fun(): String = "1"<!>

    konst a12: Int = <!INITIALIZER_TYPE_MISMATCH!>fun(): String = "1"<!>
    konst a13: Int = <!INITIALIZER_TYPE_MISMATCH!>fun() = fun(): String = "1"<!>
    konst a14: Int = <!INITIALIZER_TYPE_MISMATCH!>fun() = fun() = "1"<!>
    konst a15: Int = <!INITIALIZER_TYPE_MISMATCH!>fun() = fun() = {}<!>
    konst a16: Int = <!INITIALIZER_TYPE_MISMATCH!>fun() = fun() {}<!>

    konst a17: () -> Unit = fun() {}
    konst a18: () -> Int = <!INITIALIZER_TYPE_MISMATCH!>fun() {}<!>
    konst a19: () -> () -> Int = <!INITIALIZER_TYPE_MISMATCH!>fun() = fun() {}<!>
    konst a20: () -> () -> () -> Unit = fun() = fun() = {}
    konst a21: () -> () -> () -> Int = <!INITIALIZER_TYPE_MISMATCH!>fun() = fun() = {}<!>
}
