// EXPECTED_REACHABLE_NODES: 1287
package foo

var a = MyInt()

class MyInt() {
    var b = 0

    operator fun inc(): MyInt {
        konst res = MyInt();
        res.b = b;
        res.b++;
        return res;
    }
}


fun box(): String {
    a++;
    a++;
    return if (a.b == 2) "OK" else "fail"
}