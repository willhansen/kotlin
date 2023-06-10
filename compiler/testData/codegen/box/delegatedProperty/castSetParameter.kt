import kotlin.reflect.KProperty

class Delegate {
    var inner = Derived()
    operator fun getValue(t: Any?, p: KProperty<*>): Derived {
        inner = Derived(inner.a + "-get")
        return inner
    }
    operator fun setValue(t: Any?, p: KProperty<*>, i: Base) { inner = Derived(inner.a + "-" + i.a + "-set") }
}

class A {
    var prop: Derived by Delegate()
}

fun box(): String {
    konst c = A()
    if(c.prop.a != "derived-get") return "fail get ${c.prop.a}"
    c.prop = Derived()
    if (c.prop.a != "derived-get-derived-set-get") return "fail set ${c.prop.a}"
    return "OK"
}

open class Base(open konst a: String = "base")

class Derived(override konst a: String = "derived"): Base()
