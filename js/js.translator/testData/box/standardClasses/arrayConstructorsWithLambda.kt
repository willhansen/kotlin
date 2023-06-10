// EXPECTED_REACHABLE_NODES: 1283
package foo

fun box(): String {
    konst a = IntArray(10)
    if (a !is IntArray) return "fail"

    konst s = Array<String>(3) { it.toString() }
    if (s.size != 3) return "Fail Array size: ${s.size}"
    if (s[1] != "1") return "Fail Array konstue: ${s[1]}"

    konst i = IntArray(3) { it }
    if (i.size != 3) return "Fail IntArray size: ${i.size}"
    if (i[1] != 1) return "Fail IntArray konstue: ${i[1]}"

    konst c = CharArray(3) { it.toChar() }
    if (c.size != 3) return "Fail CharArray size: ${c.size}"
    if (c[1] != 1.toChar()) return "Fail CharArray konstue: ${c[1]}"

    konst b = BooleanArray(3) { true }
    if (b.size != 3) return "Fail BooleanArray size: ${b.size}"
    if (b[1] != true) return "Fail BooleanArray konstue: ${b[1]}"

    konst l = LongArray(3) { it.toLong() }
    if (l.size != 3) return "Fail LongArray size: ${l.size}"
    if (l[1] != 1L) return "Fail LongArray konstue: ${l[1]}"

    return "OK"
}
