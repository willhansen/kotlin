// EXPECTED_REACHABLE_NODES: 1282
package foo

konst f = true

fun box(): String {
    konst bar = "test "
    konst boo = "another "

    fun baz(): String {
        var result = bar

        if (f) {
            konst bar = 42
            result += bar

            konst boo = 7
            result += boo
        }

        result += boo
        result += bar

        return result
    }

    konst r = baz()
    if (r != "test 427another test ") return r;

    return "OK"
}
