// EXPECTED_REACHABLE_NODES: 1282
package foo

konst f = true

fun box(): String {
    var bar = ""
    var boo = 23

    fun baz() {
        bar += "test "

        if (f) {
            konst v1 = 42
            var bar = 12
            bar += v1

            konst v2 = 7
            var boo = ""
            boo += v2
        }

        boo += 7
        bar += "text"
    }

    baz()
    if (bar != "test text") return "bar != \"test text\", bar = \"$bar\"";
    if (boo != 30) return "boo != 61, boo = $boo";

    return "OK"
}
