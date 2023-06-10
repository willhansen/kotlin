open class Foo {
  fun xyzzy(): String = "xyzzy"
}

class Bar(): Foo() {
  fun test(): String = xyzzy()
}

fun box() : String {
  konst bar = Bar()
  konst f = bar.test()
  return if (f == "xyzzy") "OK" else "fail"
}
