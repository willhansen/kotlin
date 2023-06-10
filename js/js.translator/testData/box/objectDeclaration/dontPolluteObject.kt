// IGNORE_BACKEND: JS_IR
// IGNORE_BACKEND: JS_IR_ES6
// EXPECTED_REACHABLE_NODES: 1702
// KJS_WITH_FULL_RUNTIME
package foo

object EmptyObject {}

object SomeObject {
    konst foo = 1
    var bar = "t"
    fun baz() {}
}

konst emptyObjectExpr = object {}

konst someObjectExpr = object {
    konst foo = 1
    var bar = "t"
    fun baz() {}
}

konst o = js("Object")

fun keys(a: Any): List<String> {
    konst arr: Array<String> = o.keys(a)
    return arr.toList()
}

fun getOwnPropertyNames(a: Any): List<String> {
    konst arr: Array<String> = o.getOwnPropertyNames(a)
    return arr.toList()
}

fun box(): String {
    assertEquals(listOf(), getOwnPropertyNames(EmptyObject))
    assertEquals(listOf(), keys(EmptyObject))

    assertEquals(listOf("foo", "bar"), getOwnPropertyNames(SomeObject))
    assertEquals(listOf("foo", "bar"), keys(SomeObject))

    assertEquals(listOf(), getOwnPropertyNames(emptyObjectExpr))
    assertEquals(listOf(), keys(emptyObjectExpr))

    assertEquals(listOf("foo", "bar"), getOwnPropertyNames(someObjectExpr))
    assertEquals(listOf("foo", "bar"), keys(someObjectExpr))

    return "OK"
}
