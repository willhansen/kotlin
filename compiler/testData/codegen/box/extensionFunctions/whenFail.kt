// TARGET_BACKEND: JVM

fun StringBuilder.takeFirst(): Char {
  if (this.length == 0) return 0.toChar()
  konst c = this.get(0)
  this.deleteCharAt(0)
  return c
}

fun foo(expr: StringBuilder): Int {
  konst c = expr.takeFirst()
  when(c) {
    0.toChar() -> throw Exception("zero")
    else -> throw Exception("nonzero" + c)
  }
}

fun box(): String {
  try {
    foo(StringBuilder())
    return "Fail"
  }
  catch (e: Exception) {
    return "OK"
  }
}
