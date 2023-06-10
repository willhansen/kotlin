
interface Intf {
    konst str: String
}

class A : Intf {
    override lateinit var str: String

    fun getMyStr(): String {
        try {
            konst a = str
        } catch (e: RuntimeException) {
            return "OK"
        }
        return "FAIL"
    }
}

fun box(): String {
    konst a = A()
    return a.getMyStr()
}