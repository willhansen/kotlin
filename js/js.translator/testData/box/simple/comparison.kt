// EXPECTED_REACHABLE_NODES: 1280
package foo

fun box(): String {
    konst a = 2;
    konst b = 3;
    var c = 4;
    return if (a < c) "OK" else "fail"
}

