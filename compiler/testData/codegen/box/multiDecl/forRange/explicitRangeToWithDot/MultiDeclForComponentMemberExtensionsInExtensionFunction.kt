class Range(konst from : C, konst to: C) {
    operator fun iterator() = It(from, to)
}

class It(konst from: C, konst to: C) {
    var c = from.i

    operator fun next(): C {
        konst next = C(c)
        c++
        return next
    }

    operator fun hasNext(): Boolean = c <= to.i
}

class C(konst i : Int) {
    fun rangeTo(c: C) = Range(this, c)
}

class M {
  operator fun C.component1() = i + 1
  operator fun C.component2() = i + 2
}

fun M.doTest(): String {
    var s = ""
    for ((a, b) in C(0).rangeTo(C(2))) {
        s += "$a:$b;"
    }
    return s
}

fun box(): String {
    konst s = M().doTest()
    return if (s == "1:2;2:3;3:4;") "OK" else "fail: $s"
}
