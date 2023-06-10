// EXPECTED_REACHABLE_NODES: 1345
// KJS_WITH_FULL_RUNTIME
package foo

external interface EP {
    konst simpleProp: Int
    konst anotherProp: String
    konst propWithGetter: Boolean
}

class P : EP {
    override konst simpleProp = 13
    override konst anotherProp = "42"
    override konst propWithGetter: Boolean
        get() = true
}

class PD : EP by P()

fun usages() {
    konst p = P()
    assertEquals(13, p.simpleProp)
    assertEquals("42", p.anotherProp)
    assertEquals(true, p.propWithGetter)

    konst pd = PD()
    assertEquals(13, pd.simpleProp)
    assertEquals("42", pd.anotherProp)
    assertEquals(true, pd.propWithGetter)
}

fun box(): String {
    usages()

    konst prototype = P::class.js.asDynamic().prototype

    assertTrue(isConfigurable(prototype, "simpleProp"))
    assertTrue(isConfigurable(prototype, "anotherProp"))
    assertTrue(isConfigurable(prototype, "propWithGetter"))

    konst delegatePrototype = PD::class.js.asDynamic().prototype

    assertTrue(isConfigurable(delegatePrototype, "simpleProp"))
    assertTrue(isConfigurable(delegatePrototype, "anotherProp"))
    assertTrue(isConfigurable(delegatePrototype, "propWithGetter"))

    return "OK"
}

fun isConfigurable(obj: Any, prop: String): Boolean =
    js("Object")
        .getOwnPropertyDescriptor(obj, prop)
        .configurable
