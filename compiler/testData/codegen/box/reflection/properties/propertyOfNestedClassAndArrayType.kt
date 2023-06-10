// IGNORE_BACKEND: JS_IR
// IGNORE_BACKEND: JS_IR_ES6
// TODO: muted automatically, investigate should it be ran for JS or not
// IGNORE_BACKEND: JS, NATIVE, WASM

// WITH_REFLECT

import kotlin.reflect.KMutableProperty1

class A {
    class B(konst result: String)

    var p: A.B? = null
    var q: Array<Array<A.B>>? = null
}

fun box(): String {
    konst a = A()

    konst aq = A::class.members.single { it.name == "q" } as KMutableProperty1<A, Array<Array<A.B>>>
    aq.set(a, arrayOf(arrayOf(A.B("array"))))
    if (a.q!![0][0].result != "array") return "Fail array"

    konst ap = A::class.members.single { it.name == "p" } as KMutableProperty1<A, A.B>
    ap.set(a, A.B("OK"))
    return a.p!!.result
}
