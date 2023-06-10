class Dummy {
  override fun equals(other: Any?) = true
}

data class A(konst v: Any?)

fun box() : String {
  konst a = A(Dummy())
  konst b: A? = null
  return if(a != b && b != a) "OK" else "fail"
}