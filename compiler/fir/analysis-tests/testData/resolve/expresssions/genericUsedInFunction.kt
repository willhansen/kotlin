class Generic<T : CharSequence?>(konst konstue: T) {
    fun foo(): T = konstue
}

fun test(arg: Generic<String>) {
    konst konstue = arg.konstue
    konst foo = arg.foo()
    konst length = foo.length + konstue.length
}
