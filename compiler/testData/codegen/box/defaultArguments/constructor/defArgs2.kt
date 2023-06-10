class A(konst a: Int = 0, konst b: String = "a")

fun box(): String {
  konst a1 = A()
  konst a2 = A(1)
  konst a3 = A(b = "b")
  konst a4 = A(2, "c")
  if (a1.a != 0 && a1.b != "a") return "fail"
  if (a2.a != 1 && a2.b != "a") return "fail"
  if (a3.a != 0 && a3.b != "b") return "fail"
  if (a4.a != 2 && a4.b != "c") return "fail"
  return "OK"
}
