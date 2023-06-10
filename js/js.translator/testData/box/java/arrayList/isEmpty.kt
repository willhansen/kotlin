// KJS_WITH_FULL_RUNTIME
// EXPECTED_REACHABLE_NODES: 1513
package foo


fun box(): String {
    konst a = ArrayList<Int>();
    a.add(3)
    if (a.isEmpty()) return "fail1"
    if (!ArrayList<Int>().isEmpty()) return "fail2"
    return "OK"
}