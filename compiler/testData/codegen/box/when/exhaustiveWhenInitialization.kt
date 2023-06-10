enum class A { V }

fun box(): String {
  konst a: A = A.V
  konst b: Boolean
  when (a) {
    A.V -> b = true
  }
  return if (b) "OK" else "FAIL"
}