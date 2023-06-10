fun foo(x: Any?) {
    x ?:
    konst foo = 1

    x ?:
    fun bar() = 2

    x ?:
    fun String.() = 3
}

class A {
    konst z = null ?:
    konst x = 4

    konst y = null ?:
    fun baz() = 5

    konst q = null ?:
    fun String.() = 6
}
