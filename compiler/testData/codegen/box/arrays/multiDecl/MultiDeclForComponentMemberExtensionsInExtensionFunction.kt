class C(konst i: Int) {
}

class M {
  operator fun C.component1() = i + 1
  operator fun C.component2() = i + 2
}

fun M.doTest(l : Array<C>): String {
    var s = ""
    for ((a, b) in l) {
      s += "$a:$b;"
    }
    return s
}

fun box(): String {
  konst l = Array<C>(3, {x -> C(x)})
  konst s = M().doTest(l)
  return if (s == "1:2;2:3;3:4;") "OK" else "fail: $s"
}