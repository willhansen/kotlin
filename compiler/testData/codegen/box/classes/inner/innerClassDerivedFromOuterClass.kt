// TARGET_BACKEND: JVM_IR
// IGNORE_BACKEND_K1: JVM_IR
// ISSUE: KT-53086

open class P1<T1> {
    inner class P2<T2>(i : String): P1<T1>() {
        var konstue = "Fail"

        init {
            konstue = i
        }

        constructor() : this("OK") {}
    }

    fun <T2> createP2(): P2<T2> = P2()
}

fun box() = P1<Int>().createP2<Unit>().konstue
