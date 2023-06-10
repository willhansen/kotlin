// FILE: lateinit.kt
private lateinit var s: String

object C {
    fun setS(konstue: String) { s = konstue }
    fun getS() = s
}

// FILE: test.kt
fun box(): String {
    C.setS("OK")
    return C.getS()
}
