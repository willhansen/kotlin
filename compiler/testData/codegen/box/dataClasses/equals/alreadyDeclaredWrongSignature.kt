// TARGET_BACKEND: JVM

// WITH_STDLIB

data class B(konst x: Int) {
  fun equals(other: B): Boolean = false
}

data class C(konst x: Int) {
  fun equals(): Boolean = false
}

data class D(konst x: Int) {
  fun equals(other: Any?, another: String): Boolean = false
}

data class E(konst x: Int) {
  fun equals(x: E): Boolean = false
  override fun equals(x: Any?): Boolean = false
}

fun box(): String {
  B::class.java.getDeclaredMethod("equals", Any::class.java)
  B::class.java.getDeclaredMethod("equals", B::class.java)

  C::class.java.getDeclaredMethod("equals", Any::class.java)
  C::class.java.getDeclaredMethod("equals")

  D::class.java.getDeclaredMethod("equals", Any::class.java)
  D::class.java.getDeclaredMethod("equals", Any::class.java, String::class.java)

  E::class.java.getDeclaredMethod("equals", Any::class.java)
  E::class.java.getDeclaredMethod("equals", E::class.java)

  return "OK"
}
