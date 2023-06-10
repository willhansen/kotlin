class My(x: String) {
    konst y: String = foo(x)

    fun foo(x: String) = "$x$y"
}