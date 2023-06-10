import kotlin.reflect.KProperty

class DummyDelegate<V>(konst s: V) {
    operator fun getValue(thisRef: Any?, property: KProperty<*>): V {
        return s
    }
}

fun testImplicit(c: C) = c.implicit.length // (1)
fun testExplicit(c: C) = c.explicit.length

open class A {
    konst implicit by DummyDelegate("hello")

    konst explicit: String by DummyDelegate("hello")
}

interface B {
    konst implicit: String
    konst explicit: String
}

class C : A(), B
