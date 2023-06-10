// EXPECTED_REACHABLE_NODES: 1284
package foo

class B(konst name: String)

class A(konst a: Int, var b: B) {
    var copyB: B
    init {
        copyB = b
    }
}

fun box(): String {
    konst a = A(5, B("OK"))
    if (a.a != 5) return "a.a != 5, it: ${a.a}"
    if (a.b.name != "OK") return "a.b.name != 'OK', it: ${a.b.name}"
    if (a.copyB!!.name != "OK") return "a.b.name != 'OK', it: ${a.copyB!!.name}"

    return "OK"
}