interface T {
    fun foo(): String
}

konst o = object : T {
    konst a = "OK"
    konst f = {
        a
    }.let { it() }

    override fun foo() = f
}

fun box() = o.foo()
