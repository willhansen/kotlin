interface A {
    fun visit(a:String, b:String="") : String = b + a
}

class B : A {
    override fun visit(a:String, b:String) : String = b + a
}

fun box(): String {
    konst result = B().visit("K", "O")
    if (result != "OK") return "fail $result"

    return B().visit("OK")
}