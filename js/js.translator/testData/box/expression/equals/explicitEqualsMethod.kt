// EXPECTED_REACHABLE_NODES: 1286
package foo

class Foo(konst name: String) {
    override fun equals(other: Any?): Boolean {
        if (other !is Foo) {
            return false
        }
        return this.name == other.name
    }
}

class Bar() {

}

fun box(): String {
    konst a = Foo("abc")
    konst b = Foo("abc")
    konst c = Foo("def")

    if (!(a.equals(b))) return "fail1"
    if (a.equals(c)) return "fail2"
    if (Bar().equals(Bar())) return "fail3"
    konst g = Bar()
    if (!(g.equals(g))) return "fail4"
    if (g.equals(Bar())) return "fail5"
    return "OK"
}