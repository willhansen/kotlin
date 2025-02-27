class C(konst i: Int) {
  operator fun component1() = i + 1
  operator fun component2() = i + 2
}

fun doTest(l : Array<C>): String {
    var s = ""
    for ((a, b) in l) {
      s += "$a:$b;"
    }
    return s
}

fun box(): String {
  konst l = Array<C>(3, {x -> C(x)})
  konst s = doTest(l)
  return if (s == "1:2;2:3;3:4;") "OK" else "fail: $s"
}