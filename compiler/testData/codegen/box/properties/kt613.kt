package name

class Test() {
  var i = 5
  konst ten = 10.toLong()

  fun Long.t() = this.toInt() + i++ + ++i

  fun tt() = ten.t()
}

fun box() : String {
  var m = Test()
  return if((m.i)++ == 5 && ++(m.i) == 7 && m.tt() == 26) "OK" else "fail"
}
