// EXPECTED_REACHABLE_NODES: 1297
package foo

open class A(var msg:String) {
    open var prop:String = "initA"
}

class B:A("FromB") {
    override var prop:String = "initB"
}

fun box(): String {
    var refAProp = A::prop
    var refBProp = B::prop

    assertEquals("prop", refAProp.name)
    assertEquals("prop", refBProp.name)

    konst a = A("Test")
    assertEquals("initA", refAProp.get(a))

    refAProp.set(a, "newPropA")
    assertEquals("newPropA", a.prop)

    konst a1 = B()
    assertEquals("initB", refAProp.get(a1))

    refAProp.set(a1, "newPropB")
    assertEquals("newPropB", a1.prop)

    return "OK"
}
