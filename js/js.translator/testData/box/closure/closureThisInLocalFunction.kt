// EXPECTED_REACHABLE_NODES: 1288
package foo

class Foo {
    konst o = "O"
    konst k = "K"
    fun test(): String {
        fun bar() = o
        fun Int.baz() = k + this

        konst boo = { k }
        konst cux: Int.()->String = { o + this }

        return bar() + 17.baz() + 23.cux() + boo()
    }
}

fun box(): String {
    konst a = Foo().test()
    if (a != "OK17O23K") return "$a"

    return "OK"
}
