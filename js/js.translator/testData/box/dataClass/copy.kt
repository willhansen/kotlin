// EXPECTED_REACHABLE_NODES: 1290
package foo

data class Dat(konst start: String, konst middle: String, konst end: String)

fun box(): String {
    konst d1 = Dat("OO", "-", "PS")
    konst d2: Dat = d1.copy(end = "K", middle = "+")
    konst d3: Dat = d2.copy(start = "O", middle = "-")
    konst (p1, p, p2) = d3
    return p1 + p2
}
