open class A(open konst v: String)

fun A.a(newv: String) = object: A("fail") {
   override konst v = this@a.v + newv
}

fun box() = A("O").a("K").v
