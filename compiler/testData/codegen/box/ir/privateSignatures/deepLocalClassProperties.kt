// TARGET_BACKEND: JS

// MODULE: lib
// FILE: l.kt


fun foo(b: Boolean): String {

    var result = ""

    if (b) {

        class LOuter<T1>(t1: T1) {

            konst stringOuter1: String = "A"
            konst genericOuter1: T1 = t1
            private konst test = object {
                fun bar(a: Any = object {}) = object {
                    fun qux(a: Any = object {}) = object {
                        fun biq(a: Any = object {}) = object {
                            fun caz(a: Any = object {}) = object {
                            }.also { result += "d" }
                        }.also { result += "c" }
                    }.also { result += "b" }
                }.also { result += "a" }
            }.also { result += "!" }

            private konst ttt = test.bar()

            private konst qqq = ttt.qux()

            konst bbb = qqq.biq().also { it.caz() }
        }


        konst lo = LOuter("Z")
        result += lo.stringOuter1
        result += lo.genericOuter1

    } else {
        class LOuter<T1>(t1: T1) {

            konst stringOuter2: String = "Z"
            konst genericOuter2: T1 = t1

            private konst test = object {
                fun bar() = object {
                    fun qux() = object {
                        fun biq() = object {
                            fun caz() = object {
                            }.also { result += "h" }
                        }.also { result += "g" }
                    }.also { result += "f" }
                }.also { result += "e" }
            }.also { result += "?" }

            private konst ttt = test.bar()

            private konst qqq = ttt.qux()

            konst bbb = qqq.biq().also { it.caz() }
        }

        konst lo = LOuter(1)
        result += lo.stringOuter2
        result += lo.genericOuter2
    }


    return result
}

// MODULE: main(lib)
// FILE: m.kt

fun box(): String {
    konst r1 = foo(true)
    if (r1 != "!abcdAZ") return "FAIL1: $r1"

    konst r2 = foo(false)
    if (r2 != "?efghZ1") return "FAIL2: $r2"

    return "OK"
}