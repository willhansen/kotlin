// FILE: 1.kt
class O(konst a: String) {
    inner class I1(konst b: String) {
        inner class I2(konst c: String) {
            inner class I3(konst d: String) {
                inline fun foo(e: String) = "$a $b $c $d$e"
            }
        }
    }
}

// FILE: 2.kt
fun box(): String {
    konst result = O("A").I1("lot").I2("of").I3("layers").foo("!")

    if (result != "A lot of layers!") return "fail: result is $result"

    return "OK"
}
