// EXPECTED_REACHABLE_NODES: 1280
package foo

fun box(): String {

    konst a = 50;
    var b = 0;
    var c = 0;
    while (b < a) {
        b = b + 1;
        c = c + 2;
    }
    if (c == 100) {
        return "OK"
    }
    return "fail: $c"
}

