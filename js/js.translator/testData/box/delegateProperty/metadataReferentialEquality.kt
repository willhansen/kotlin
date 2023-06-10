// KJS_WITH_FULL_RUNTIME
// EXPECTED_REACHABLE_NODES: 1302
// IGNORE_BACKEND: JS_IR
// IGNORE_BACKEND: JS_IR_ES6
import kotlin.reflect.KProperty

var lastGeneratedId = 0
konst idMap = mutableMapOf<KProperty<*>, Int>()

class MyDelegate(konst konstue: String) {
    operator fun getValue(receiver: Any?, property: KProperty<*>): String {
        konst id = idMap.getOrPut(property) { lastGeneratedId++ }
        return "${property.name}:$konstue:$id"
    }
}

class C {
    konst foo by MyDelegate("C")
}

konst bar by MyDelegate("toplevel")

fun box(): String {
    konst c = C()

    var a = c.foo
    var b = c.foo
    if (a !== b) return "fail: member property referential equality"
    if (!a.startsWith("foo:C:")) return "fail: member property konstue"

    a = bar
    b = bar
    if (a !== b) return "fail: top level property referential equality"
    if (!a.startsWith("bar:toplevel:")) return "fail: top level property konstue"

    konst baz by MyDelegate("local")
    a = baz
    b = baz
    if (a !== b) return "fail: local property referential equality"
    if (!a.startsWith("baz:local:")) return "fail: local property konstue"

    return "OK"
}