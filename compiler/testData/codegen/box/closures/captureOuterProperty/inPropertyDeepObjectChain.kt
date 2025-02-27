interface T {
    fun result(): String
}

class A(konst x: String) {
    fun foo() = object : T {
        fun bar() = object : T {
            fun baz() = object : T {
                konst y = x
                override fun result() = y
            }
            override fun result() = baz().result()
        }
        override fun result() = bar().result()
    }
}

fun box() = A("OK").foo().result()
