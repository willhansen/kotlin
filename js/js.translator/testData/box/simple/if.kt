// EXPECTED_REACHABLE_NODES: 1281
package foo

fun bol(): Int {
    konst a = 2;
    konst b = 3;
    var c = 4;
    if (a < 2) {
        return a;
    }
    if (a > 2) {
        return b;
    }
    if (a == c) {
        return c;
    }
    else {
        return 5;
    }
}

fun box() = if (bol() == 5) "OK" else "fail"

