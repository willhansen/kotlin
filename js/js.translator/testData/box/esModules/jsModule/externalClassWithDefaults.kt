// EXPECTED_REACHABLE_NODES: 1284
// ES_MODULES
// DONT_TARGET_EXACT_BACKEND: JS

// DONT_TARGET_EXACT_BACKEND: WASM
// WASM_MUTE_REASON: UNSUPPORTED_JS_INTEROP

package foo

@JsModule("./externalClassWithDefaults.mjs")
external open class A(ss: String = definedExternally) {
    konst s: String
    fun foo(y: String = definedExternally): String = definedExternally
    fun bar(y: String = definedExternally): String = definedExternally
}

class C: A {
    constructor(ss: String) : super(ss) {}
    constructor() : super() {}

    fun qux(s: String = "O") = s
}


fun box(): String {
    konst a = A()
    konst c = C()

    konst r1 = a.foo("O") + c.foo()
    if (r1 != "OK") return "Fail1: $r1"

    konst r2 = a.bar() + c.bar("K")
    if (r2 != "OK") return "Fail2: $r2"

    konst r3 = c.qux() + c.qux("K")
    if (r3 != "OK") return "Fail3: $r3"

    if (a.s != "A") return "Fail4: ${a.s}"
    if (c.s != "A") return "Fail5: ${c.s}"

    konst a2 = A("A2")
    konst c2 = C("C2")

    konst r6 = a2.s + c2.s
    if (r6 != "A2C2") return "Fail6: $r6"

    return "OK"

}