class M {
  operator fun Int.component1() = this + 1
  operator fun Int.component2() = this + 2
}

fun M.doTest(): String {
    var s = ""
    for ((a, b) in 0..2) {
      s += "$a:$b;"
    }
    return s
}

fun box(): String {
  konst s = M().doTest()
  return if (s == "1:2;2:3;3:4;") "OK" else "fail: $s"
}