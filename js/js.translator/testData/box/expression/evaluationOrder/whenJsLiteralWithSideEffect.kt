// EXPECTED_REACHABLE_NODES: 1289
package foo

var global: String = ""

fun bar(s: String): Int {
    global += s
    return 1
}
fun testWhen() {
    global = ""
    when(arrayOf(bar("A"),2,3)) {
        arrayOf(1) -> bar("1")
        arrayOf(2) -> bar("2")
        else  -> bar("else")
    }
    assertEquals("Aelse", global)

}

fun testIntrinsic() {
    global = ""
    konst x = arrayOf(bar("A")) == try { arrayOf(bar("B")) } finally {}
    assertEquals("AB", global)
}

fun testElvis() {
    global = ""
    var x = arrayOf(bar("A")) ?: 10
    assertEquals("A", global)
}

fun box(): String {
    testWhen()
    testIntrinsic()
    testElvis()

    return "OK"
}
