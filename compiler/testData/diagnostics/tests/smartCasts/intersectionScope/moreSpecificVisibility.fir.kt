abstract class A {
    abstract protected fun foo(): String
    abstract protected konst bar: String
}

interface B {
    fun foo(): String
    konst bar: String
}

fun test(x: A) {
    if (x is B) {
        x.foo()
        x.bar
    }
}
