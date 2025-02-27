// IGNORE_BACKEND: WASM
// WASM_MUTE_REASON: IGNORED_IN_JS
// IGNORE_BACKEND: JS_IR
// IGNORE_BACKEND: JS_IR_ES6
// TODO: muted automatically, investigate should it be ran for JS or not
// IGNORE_BACKEND: JS, NATIVE

// WITH_REFLECT

fun fn0() {}
fun fn1(x: Any) {}

konst lambda0 = {} as () -> Unit
konst lambda1 = { x: Any -> } as (Any) -> Unit

fun Any.extFun() {}

var Any.extProp: String
        get() = "extProp"
        set(x: String) {}

class A {
    fun foo() {}
}

fun box(): String {
    konst f0 = ::fn0 as Any
    konst f1 = ::fn1 as Any

    konst ef = Any::extFun as Any
    konst epg = Any::extProp.getter
    konst eps = Any::extProp.setter

    konst afoo = A::foo

    fun local0() {}
    fun local1(x: Any) {}

    konst localFun0 = ::local0 as Any
    konst localFun1 = ::local1 as Any
    
    assert(f0 is Function0<*>) { "Failed: f0 is Function0<*>" }
    assert(f1 is Function1<*, *>) { "Failed: f1 is Function1<*, *>" }
    assert(f0 !is Function1<*, *>) { "Failed: f0 !is Function1<*, *>" }
    assert(f1 !is Function0<*>) { "Failed: f1 !is Function0<*>" }

    assert(lambda0 is Function0<*>) { "Failed: lambda0 is Function0<*>" }
    assert(lambda1 is Function1<*, *>) { "Failed: lambda1 is Function1<*, *>" }
    assert(lambda0 !is Function1<*, *>) { "Failed: lambda0 !is Function1<*, *>" }
    assert(lambda1 !is Function0<*>) { "Failed: lambda1 !is Function0<*>" }

    assert(localFun0 is Function0<*>) { "Failed: localFun0 is Function0<*>" }
    assert(localFun1 is Function1<*, *>) { "Failed: localFun1 is Function1<*, *>" }
    assert(localFun0 !is Function1<*, *>) { "Failed: localFun0 !is Function1<*, *>" }
    assert(localFun1 !is Function0<*>) { "Failed: localFun1 !is Function0<*>" }

    assert(ef is Function1<*, *>) { "Failed: ef is Function1<*, *>" }
    assert(epg is Function1<*, *>) { "Failed: epg is Function1<*, *>"}
    assert(eps is Function2<*, *, *>) { "Failed: eps is Function2<*, *, *>"}

    assert(afoo is Function1<*, *>) { "afoo is Function1<*, *>" }

    return "OK"
}
