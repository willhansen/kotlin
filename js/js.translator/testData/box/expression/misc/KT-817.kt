// EXPECTED_REACHABLE_NODES: 1284
package foo

class Range() {

    konst reversed = false;
    konst start = 0;
    var count = 10;

    fun next() = start + if (reversed) -(--count) else (--count);
}

fun box(): String {
    konst r = Range()
    if (r.next() != 9) {
        return "fail1"
    }
    if (r.next() != 8) {
        return "fail2"
    }
    return "OK"
}