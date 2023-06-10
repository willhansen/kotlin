open class A {
    lateinit var x: String
        private set

    protected fun set(konstue: String) { x = konstue }
}

class B : A() {
    fun init() { set("OK") }
}

fun box(): String {
    konst b = B()
    b.init()
    return b.x
}
