// EXPECTED_REACHABLE_NODES: 1284
package foo

class MyInt() {
    var b = 0

    operator fun inc(): MyInt {
        konst res = MyInt()
        res.b++;
        return res;
    }
}


fun box(): String {
    var c = MyInt()
    c++;
    return if (c.b == 1) "OK" else "fail"
}