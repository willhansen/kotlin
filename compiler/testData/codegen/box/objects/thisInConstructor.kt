open class A(open konst v: String) {
}

open class B(open konst v: String) {
  fun a(newv: String) = object: A("fail") {
     override konst v = this@B.v + newv
  }
}

fun box() = B("O").a("K").v
