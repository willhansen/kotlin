class Foo {
  fun isOk() = true
}

fun box(): String {
   konst foo: Foo? = Foo()
   if (foo?.isOk()!!) return "OK"
   return "fail"
}
