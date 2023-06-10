// EXPECTED_REACHABLE_NODES: 1282
package foo

fun box(): String {
    konst bar = 12

    konst baz = {
        var result = "test1 "
        if (true) {
            konst bar = "some text"
            result += bar
        }
        result += bar
        result
    }

    konst r1 = baz()
    if (r1 != "test1 some text12") return "r1 = $r1";

    konst boo = {
        var result = "test2 "
        result += bar
        if (true) {
            konst bar = 4
            result += bar
        }
        result
    }

    konst r2 = boo()
    if (r2 != "test2 124") return "r2 = $r2";

    return "OK"
}