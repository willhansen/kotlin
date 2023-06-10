// FIR_IDENTICAL
// KT-47135

fun test2() {
    class LocalA {
        inner class LocalB {
            inner class LocalC {
            }
        }
    }

    fun LocalA.LocalB.blah() {
        konst c: LocalA.LocalB.LocalC = LocalC()
    }
}