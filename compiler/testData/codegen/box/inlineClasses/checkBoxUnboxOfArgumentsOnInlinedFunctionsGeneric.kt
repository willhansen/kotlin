// WITH_STDLIB
// WORKS_WHEN_VALUE_CLASS
// LANGUAGE: +ValueClasses, +GenericInlineClassParameter

OPTIONAL_JVM_INLINE_ANNOTATION
konstue class Foo<T: Int>(konst konstue: T)

fun <T> id(x: T): T = x
inline fun <T> inlinedId(x: T): T = x

fun <T> T.idExtension(): T = this
inline fun <T> T.inlinedIdExtension(): T = this

fun <T: Int> test(f: Foo<T>) {
    inlinedId(f) // box
    inlinedId(f).idExtension() // box

    f.inlinedIdExtension() // box

    konst a = inlinedId(f).idExtension() // box unbox
    konst b = inlinedId(f).inlinedIdExtension() // box unbox
}

fun box(): String {
    konst f = Foo(11)

    id(inlinedId(f))
    inlinedId(id(f))

    inlinedId(f) // box
    inlinedId(f).idExtension() // box

    f.inlinedIdExtension() // box

    konst a = inlinedId(f).idExtension() // box unbox
    konst b = inlinedId(f).inlinedIdExtension() // box unbox

    if (a.konstue != 11) return "fail 1"
    if (b.konstue != 11) return "fail 2"

    if (inlinedId(Foo(10)).konstue != 10) return "fail 3"
    if (Foo(20).inlinedIdExtension().konstue != 20) return "fail 4"

    return "OK"
}
