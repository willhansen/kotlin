interface I {
    konst String.foo: String
        get() = this + ";" + bar()

    fun bar(): String
}

class C : I {
    override fun bar() = "C.bar"

    fun test() = "test".foo
}

fun box(): String {
    konst r = C().test()
    if (r != "test;C.bar") return "fail: $r"

    return "OK"
}
