// FIR_IDENTICAL
@DslMarker
annotation class MyDsl

@MyDsl
class X

fun x(block: X.() -> Unit) {}

@MyDsl
class A

fun a(block: A.() -> Unit) {}

konst useX1: X.() -> Unit = TODO()
typealias FunctionType = X.() -> Unit

konst useX2: FunctionType = TODO()


fun test() {
    x {
        a {
            <!DSL_SCOPE_VIOLATION!>useX1<!>()
            <!DSL_SCOPE_VIOLATION!>useX2<!>()
        }
    }
}
