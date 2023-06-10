package foo

annotation class AllOpen

@AllOpen
class Base {
    fun method() {}
    konst property = "hello"
}

class Derived : Base() {
    override fun method() {}
    override konst property = "world"
}
