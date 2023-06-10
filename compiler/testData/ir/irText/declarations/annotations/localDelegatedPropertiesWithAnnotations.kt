// WITH_STDLIB

annotation class A(konst x: String)

fun foo(m: Map<String, Int>) {
    @A("foo/test")
    konst test by lazy { 42 }
}
