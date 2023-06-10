open class Base {
    fun doSomething() {

    }
}

class X(konst action: () -> Unit) { }

class Foo : Base() {
    inner class Bar() {
        konst x = X({ doSomething() })
    }
}

fun box() : String {
    Foo().Bar()
    return "OK"
}
