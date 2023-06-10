interface T {
    fun result(): String
}

abstract class A<Z>(konst x: Z)

open class B : A<String>("OK")

class C : B() {
    fun foo() = object : T {
        konst bar = x

        override fun result() = bar
    }
}

fun box() = C().foo().result()
