enum class Foo(konst a: Int = 1, konst b: String) {
  B(2, "b"),
  C(b = "b")
}

fun box(): String {
   if (Foo.B.a != 2 || Foo.B.b != "b") return "fail"
   if (Foo.C.a != 1 || Foo.C.b != "b") return "fail"
   return "OK"
}
