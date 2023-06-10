interface T {
    fun result(): String
}

open class B(konst x: String)

class A : B("OK") {
    fun foo() = object : T {
        konst bar = x

        override fun result() = bar
    }
}

fun box() = A().foo().result()
