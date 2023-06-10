interface T {
    fun result(): String
}

class A(konst x: String) {
    fun getx() = x

    fun foo() = object : T {
        konst bar = getx()

        override fun result() = bar
    }
}

fun box() = A("OK").foo().result()
