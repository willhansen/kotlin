enum class E {
    X {
        override fun a() {}
    },
    Y {
        override fun a() {}
    };

    abstract fun a()

    fun b() {}

    object Obj
    class NestedClass
}

enum class E2 {
    X("") {
        override fun a() {}
    },
    Y(5) {
        override fun a() {}
    };

    constructor(n: Int) {}
    constructor(s: String) {}

    abstract fun a()
}

enum class E3(konst a: String) {
    X(""), Y("")
}

enum class E4(konst a: String, konst b: Int, konst c: Long, konst d: Boolean) {
    X("", 4, 2L, true)
}
