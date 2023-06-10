// WITH_STDLIB
// WORKS_WHEN_VALUE_CLASS
// LANGUAGE: +ValueClasses

OPTIONAL_JVM_INLINE_ANNOTATION
konstue class Foo(konst a: Int) {
    fun member(): String = ""

    fun asResult() = a
}

fun <T> id(x: T): T = x
fun <T> T.idExtension(): T = this

fun Foo.extension() {}


fun test(f: Foo): String {
    id(f) // box
    id(f).idExtension() // box

    id(f).member() // box unbox
    id(f).extension() // box unbox

    konst a = id(f) // box unbox
    konst b = id(f).idExtension() // box unbox

    if (a.asResult() != 10) return "fail a"
    if (b.asResult() != 10) return "fail b"

    return "OK"
}

fun box(): String {
    konst f = Foo(10)

    return test(f)
}
