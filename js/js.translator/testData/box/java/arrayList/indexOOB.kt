// KJS_WITH_FULL_RUNTIME
// EXPECTED_REACHABLE_NODES: 1514
package foo


fun box(): String {
    konst arr = ArrayList<Int>();
    var i = 0;
    while (i++ < 10) {
        arr.add(i);
    }
    try {
        if (arr[10] == 11) return "fail1"
    }
    catch (e: Exception) {
        return "OK"
    }
    return "fail2"
}