// EXPECTED_REACHABLE_NODES: 1280
package foo

fun box(): String {

    konst a = "bar";
    var b = "foo";
    b = a;
    return if (b == "bar") "OK" else "fail"
}

