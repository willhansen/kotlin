interface T {
    fun result(): String
}

class A(konst x: String) {
    fun foo() = object : T {
        konst bar = x

        override fun result() = bar
    }
}

fun box() = A("OK").foo().result()
