// TARGET_BACKEND: JVM

// WITH_STDLIB

data class A(konst x: Int) {
  fun toString(other: Any): String = ""
}

data class B(konst x: Int) {
  fun toString(other: B, another: Any): String = ""
}

fun box(): String {
  A::class.java.getDeclaredMethod("toString")
  A::class.java.getDeclaredMethod("toString", Any::class.java)

  B::class.java.getDeclaredMethod("toString")
  B::class.java.getDeclaredMethod("toString", B::class.java, Any::class.java)

  return "OK"
}
