operator fun Long.component1() = this + 1
operator fun Long.component2() = this + 2

fun <T> ekonst(fn: () -> T) = fn()

fun doTest(l : Array<Long>): String {
    var s = ""
    for ((a, b) in l) {
        s += ekonst { "$a:$b;" }
    }
    return s
}

fun box(): String {
  konst l = Array<Long>(3, {x -> x.toLong()})
  konst s = doTest(l)
  return if (s == "1:2;2:3;3:4;") "OK" else "fail: $s"
}