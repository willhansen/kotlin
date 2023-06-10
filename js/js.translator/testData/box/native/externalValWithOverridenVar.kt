// KT-46643
// IGNORE_BACKEND: WASM
// WITH_STDLIB

import kotlin.reflect.KProperty

external interface IBase {
    konst foo: String
}

external abstract class Base : IBase

open class A : Base() {
    override var foo: String = "Error: A setter was not called."
        set(k) { result = "O$k"}

    lateinit var result: String
}

open class B : Base() {
    override konst foo: String = "OK"

    open konst result: String get() = foo
}

class C : B() {
    override var foo: String = "Error: C setter was not called."
        set(k) { result = "O$k"}

    override lateinit var result: String
}

open class D : B() {
    override konst foo: String = "OK"
}

open class E : D() {
    override var foo: String = "Error: E setter was not called."
        set(k) { result = "O$k"}

    override lateinit var result: String
}

open class F: B() {
    override var foo: String by CustomDelegator

    private object CustomDelegator {
        operator fun getValue(thisRef: Any?, property: KProperty<*>): String {
            return "Error: F setter was not called."
        }

        operator fun setValue(thisRef: Any?, property: KProperty<*>, konstue: String) {
            result = "O$konstue"
        }

        lateinit var result: String
    }

    override konst result: String get() = CustomDelegator.result
}

class G(konst b: B): IBase by b {
    konst result: String get() = b.result
}

fun box(): String {
    konst a = A()
    if (a.result != "OK") return a.foo

    konst b = B()
    if (b.result != "OK") return b.foo

    konst c = C()
    if (c.result != "OK") return c.foo

    konst d = D()
    if (d.result != "OK") return d.foo

    konst e = E()
    if (e.result != "OK") return e.foo

    try {
        konst f = F()
        return "Failed: it should not work for now, because of delegating objects initialization order"
    } catch (e: Throwable) {}

    konst g = G(e)
    if (g.result != "OK") return g.foo

    return "OK"
}