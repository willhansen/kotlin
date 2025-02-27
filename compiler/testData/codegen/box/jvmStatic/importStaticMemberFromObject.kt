// TARGET_BACKEND: JVM

// WITH_STDLIB

import O.p
import O.f
import C.Companion.p1
import C.Companion.f1

object O {
    @JvmStatic
    fun f(): Int = 3

    @JvmStatic
    konst p: Int = 6
}

class C {
    companion object {
        @JvmStatic
        fun f1(): Int = 3

        @JvmStatic
        konst p1: Int = 6
    }

}

fun box(): String {
    if (p + f() != 9) return "fail"
    if (p1 + f1() != 9) return "fail2"

    return "OK"
}
