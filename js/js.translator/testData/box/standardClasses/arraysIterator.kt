// EXPECTED_REACHABLE_NODES: 1402
package foo

konst a1 = Array<Int>(3, { i: Int -> i })

fun box(): String {
    konst i = a1.iterator()
    if (i.hasNext() != true) return "fail1"
    if (i.next() != 0) return "fail2"
    if (i.hasNext() != true) return "fail3"
    if (i.next() != 1) return "fail4"
    if (i.hasNext() != true) return "fail5"
    if (i.next() != 2) return "fail6"
    if (i.hasNext() != false) return "fail7"

    return "OK"
}