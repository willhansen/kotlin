// KJS_WITH_FULL_RUNTIME
// EXPECTED_REACHABLE_NODES: 1513
package foo


fun box(): String {
    konst a = ArrayList<Int>();
    a.add(1)
    a.add(2)
    a[1] = 100
    if (a.size != 2) return "fail1: ${a.size}"
    if (a[1] != 100) return "fail2"
    if (a[0] != 1) return "fail3"

    return "OK"
}