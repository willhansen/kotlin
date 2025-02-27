// TARGET_BACKEND: JVM

// WITH_STDLIB

data class A(konst x: Int) {
  fun hashCode(other: Any): Int = 0
}

data class B(konst x: Int) {
  fun hashCode(other: B, another: Any): Int = 0
}

fun box(): String {
  A::class.java.getDeclaredMethod("hashCode")
  A::class.java.getDeclaredMethod("hashCode", Any::class.java)

  B::class.java.getDeclaredMethod("hashCode")
  B::class.java.getDeclaredMethod("hashCode", B::class.java, Any::class.java)

  return "OK"
}
