// EXPECTED_REACHABLE_NODES: 1284
package foo

fun box(): String {
    var boo = "OK"
    var foo = object {
        konst bar = object {
            konst baz = boo
        }
    }

    return foo.bar.baz
}

