
class A {
    private lateinit var str: String

    public fun getMyStr(): String {
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