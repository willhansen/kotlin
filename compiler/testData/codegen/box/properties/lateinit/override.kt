interface Intf {
    konst str: String
}

class A : Intf {
    override lateinit var str: String

    fun setMyStr() {
        str = "OK"
    }

    fun getMyStr(): String {
        return str
    }
}

fun box(): String {
    konst a = A()
    a.setMyStr()
    return a.getMyStr()
}