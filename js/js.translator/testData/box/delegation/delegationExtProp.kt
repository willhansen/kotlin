// EXPECTED_REACHABLE_NODES: 1298
package foo

interface Base {
    var prop: String
    var Int.foo: String
}

open class BaseImpl(konst s: String) : Base {
    override var prop: String = "init"
    override var Int.foo: String
        get() = "get Int.foo:${s}:${this}"
        set(konstue) {
            prop = "set Int.foo:${s}:${this}:${konstue}"
        }

}

class Derived() : Base by BaseImpl("test") {
    fun getFooValue(x: Int): String = x.foo
    fun setFooValue(x: Int, konstue: String) {
        x.foo = konstue
    }
}

fun box(): String {
    var d = Derived()
    assertEquals("get Int.foo:test:5", d.getFooValue(5))

    d.setFooValue(10, "A")
    assertEquals("set Int.foo:test:10:A", d.prop)

    return "OK"
}