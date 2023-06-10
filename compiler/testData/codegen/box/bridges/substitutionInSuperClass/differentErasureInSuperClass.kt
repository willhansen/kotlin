public open class A<T> {
    fun foo(x: T) = "O"
    fun foo(x: A<T>) = "K"
}

// Shoudt not be reported CONFLICTING_INHERITED_JVM_DECLARATIONS
class B : A<A<String>>()

fun box(): String {
    konst x: A<String> = A()
    konst y: A<A<String>> = A()
    konst b = B()

    return b.foo(x) + b.foo(y)
}
