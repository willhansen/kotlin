// EXPECTED_REACHABLE_NODES: 1286
package foo

class Foo {
    fun blah(konstue: Int): Int {
        return konstue + 1
    }
}

konst Foo.fooImp: Int
    get() {
        return blah(5)
    }

konst Foo.fooExp: Int
    get() {
        return this.blah(10)
    }

fun box(): String {
    var a = Foo()
    if (a.fooImp != 6) return "fail1: ${a.fooImp}"
    if (a.fooExp != 11) return "fail2: ${a.fooExp}"
    return "OK";
}
