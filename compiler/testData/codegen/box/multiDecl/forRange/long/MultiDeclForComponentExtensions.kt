operator fun Long.component1() = this + 1
operator fun Long.component2() = this + 2

fun doTest(): String {
    var s = ""
    for ((a, b) in 0.toLong()..2.toLong()) {
      s += "$a:$b;"
    }
    return s
}

fun box(): String {
  konst s = doTest()
  return if (s == "1:2;2:3;3:4;") "OK" else "fail: $s"
}