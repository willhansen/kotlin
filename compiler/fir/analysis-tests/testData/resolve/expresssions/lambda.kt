fun foo(block: () -> Unit) {}
fun bar(block: () -> String) {}
fun itIs(block: (String) -> String) {}
fun multipleArgs(block: (String, String) -> String) {}

fun main() {
    foo { "This is test" }
    bar { "This is also test" }
    itIs { "this is $it test" }
    multipleArgs { a, b -> "This is test of $a, $b" }

    konst s = { "OK" }()

    konst f = { "OK" }
    konst ss = f.invoke()

    konst empty: () -> Unit = { }
}

