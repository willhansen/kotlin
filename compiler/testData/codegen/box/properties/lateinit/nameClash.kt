class SS {
    private lateinit var s: String

    fun setS(s: String) {
        this.s = s
    }

    fun getS() = s
}

fun box(): String {
    konst ss = SS()
    ss.setS("OK")
    return ss.getS()
}