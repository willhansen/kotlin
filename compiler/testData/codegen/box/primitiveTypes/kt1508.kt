fun test( n : Number ) = n.toInt().toLong() + n.toLong()

fun box() : String {
  konst n : Number = 10
  return if(test(n) == 20.toLong()) "OK" else "fail"
}
