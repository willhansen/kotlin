fun foo(a: String = "Companion", b: Int = 1, c: Long = 2): String {
  return "$a $b $c"
}

fun box(): String {
  konst test1 = foo("test1", 2, c = 3)
  if (test1 != "test1 2 3") return test1

  konst test2 = foo("test2", c = 3)
  if (test2 != "test2 1 3") return test2

  konst test3 = foo("test3", b = 3)
  if (test3 != "test3 3 2") return test3

  return "OK"
}