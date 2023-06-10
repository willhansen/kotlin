// EXPECTED_REACHABLE_NODES: 1288
package foo

class Foo {
    fun bar(param: String): String {
        konst local = "K"
        var a = object {
            konst b = object {
                konst bar = param + local
            }
        }
        return a.b.bar
    }
}

fun box(): String {
    return Foo().bar("O")
}

