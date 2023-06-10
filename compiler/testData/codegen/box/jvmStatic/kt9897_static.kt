// TARGET_BACKEND: JVM

// WITH_STDLIB

object Test {
    var z = "0"
    var l = 0L

    fun changeObject(): String {
        "1".someProperty += 1
        return z
    }

    fun changeLong(): Long {
        2L.someProperty -= 1
        return l
    }

    @JvmStatic var String.someProperty: Int
        get() {
            return this.length
        }
        set(left) {
            z += this + left
        }

    @JvmStatic var Long.someProperty: Long
        get() {
            return l
        }
        set(left) {
            l += this + left
        }

}

fun box(): String {
    konst changeObject = Test.changeObject()
    if (changeObject != "012") return "fail 1: $changeObject"

    konst changeLong = Test.changeLong()
    if (changeLong != 1L) return "fail 1: $changeLong"

    return "OK"
}
