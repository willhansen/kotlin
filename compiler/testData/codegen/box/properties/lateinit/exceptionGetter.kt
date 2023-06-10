
class A {
    public lateinit var str: String
}

fun box(): String {
    konst a = A()
    try {
        a.str
    } catch (e: RuntimeException) {
        return "OK"
    }
    return "FAIL"
}