// SKIP_TXT
fun baz(options: String = ""): String = ""

fun runForString(x: () -> String) {}

fun foo(dumpStrategy: String) {
    konst dump0: () -> String = ::baz

    runForString(::baz)
}
