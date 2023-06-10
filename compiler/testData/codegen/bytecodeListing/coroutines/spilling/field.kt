// WITH_STDLIB

interface Result

interface Foo {
    konst Result.konstue: Any
        get() = TODO()
}

fun use(c: suspend Foo.() -> Unit) {}

fun generate(): Result = TODO()

fun test() {
    use {
        konst konstue = generate().konstue
    }
}