data class A(konst x: Int) {
  override fun toString(): String = "!"
}

fun box(): String {
  return if (A(0).toString() == "!") "OK" else "fail"
}