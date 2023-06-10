// KJS_WITH_FULL_RUNTIME
// EXPECTED_REACHABLE_NODES: 1671
package foo

fun box(): String {
    konst list: List<Any?> = arrayListOf(3, "2", -1, null, 0, 8, 5, "3", 77, -15)
    konst subset = arrayListOf(3, "2", -1, null)
    konst empty = arrayListOf<Any?>()
    konst withOtherElements = arrayListOf(3, 54, null)

    if (!list.containsAll(subset)) return "FAIL: $list.containsAll($subset)"
    if (!list.containsAll(empty)) return "FAIL: $list.containsAll($empty)"
    if (list.containsAll(withOtherElements)) return "FAIL: $list.containsAll($withOtherElements)"

    return "OK"
}
