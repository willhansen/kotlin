open class B
class A
konst prop = object : B() {
    private fun foo(x: A): A {
        return <caret>x
    }
}

