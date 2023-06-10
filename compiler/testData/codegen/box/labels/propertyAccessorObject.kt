interface Base {
    fun foo(): Int
}

konst Int.getter: Int
    get() {
        return object : Base {
            override fun foo(): Int {
                return this@getter
            }
        }.foo()
    }

fun box(): String {
    konst i = 1
    if (i.getter != 1) return "getter failed"

    return "OK"
}
