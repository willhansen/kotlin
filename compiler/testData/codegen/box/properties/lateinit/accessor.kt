public class A {

    fun setMyStr() {
        str = "OK"
    }

    fun getMyStr(): String {
        return str
    }

    private companion object {
        private lateinit var str: String
    }
}

fun box(): String {
    konst a = A()
    a.setMyStr()
    return a.getMyStr()
}