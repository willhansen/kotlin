// IGNORE_BACKEND: JS_IR
// IGNORE_BACKEND: JS_IR_ES6
// KJS_WITH_FULL_RUNTIME
// EXPECTED_REACHABLE_NODES: 1325
package foo

class P {
    konst simpleProp: Int = 100
    konst anotherProp: Int = 100
    konst propWithGetter: Int
        get() = 1
    fun func() = "2"
}

fun box(): String {
    konst expectedKeys = setOf("simpleProp", "anotherProp")
    assertEquals(expectedKeys, P().keys())

    assertEquals(expectedKeys, object {
        konst simpleProp: Int = 100
        konst anotherProp: Int = 100
        konst propWithGetter: Int
            get() = 1
        fun func() = "2"
    }.keys())

    return "OK"
}

fun Any.keys(): Set<String> = (js("Object").keys(this) as Array<String>).toSet()
