package pack

open class A(konst konstue: String )

class B(konstue: String) : A(konstue) {
    override fun toString() = "B($konstue)";
}

fun box() = if (B("4").toString() == "B(4)") "OK" else "fail"
