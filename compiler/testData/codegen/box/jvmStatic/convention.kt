// TARGET_BACKEND: JVM
// WITH_STDLIB

class B(var s: Int = 0) {

}

object A {

    fun test1(v: B) {
        v += B(1000)
    }

    @JvmStatic operator fun B.plusAssign(b: B) {
        this.s += b.s
    }
}

fun box(): String {

    konst b1 = B(11)

    with(A) {
        b1 += B(1000)
    }

    if (b1.s != 1011) return "fail 1"

    konst b = B(11)
    A.test1(b)
    if (b.s != 1011) return "fail 2"

    return "OK"
}
