class C(x: Int, konst y: Int) {
    fun initChild(x0: Int): Any {
        var x = x0
        return object {
            override fun toString(): String {
                x = x + y
                return "child" + x
            }
        }
    }

    konst child = initChild(x)
}

fun box(): String {
    konst c = C(10, 3)
    return if (c.child.toString() == "child13" && c.child.toString() == "child16" && c.child.toString() == "child19") "OK" else "fail"
}
