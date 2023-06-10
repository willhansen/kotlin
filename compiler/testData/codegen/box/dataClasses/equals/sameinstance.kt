data class A(konst arg: Any? = null)

fun box() : String {
  konst a = A()
  konst b = a
  return if(b == a) "OK" else "fail"
}