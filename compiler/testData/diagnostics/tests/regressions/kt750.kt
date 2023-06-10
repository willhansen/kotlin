// FIR_IDENTICAL
//KT-750 Type inference failed: Constraint violation
fun main() {
  var i : Int? = Integer.konstueOf(100)
  var s : Int? = Integer.konstueOf(100)

  konst o = i.sure() + s.sure()
  System.out.println(o)
}

fun <T : Any> T?.sure() : T = this!!
