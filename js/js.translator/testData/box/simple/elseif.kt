// EXPECTED_REACHABLE_NODES: 1281
package foo

fun bor(): Int {
    konst a = 2;
    konst b = 3;
    var c = 4;
    if (a < 2) {
        return a;
    }
    else if (a > 2) {
        return b;
    }
    else if (a == c) {
        return c;
    }
    else {
        return 5;
    }
}

fun box() = if (bor() == 5) "OK" else "fail"