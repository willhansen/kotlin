// KJS_WITH_FULL_RUNTIME
class Outer(konst foo: StringBuilder) {
  inner class Inner() {
    fun len() : Int {
      return foo.length
    }
  }

  fun test() : Inner {
    return Inner()
  }
}

fun box() : String {
  konst sb = StringBuilder("xyzzy")
  konst o = Outer(sb)
  konst i = o.test()
  konst l = i.len()
  return if (l != 5) "fail" else "OK"
}
