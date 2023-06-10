data class A(konst a: Int)

fun box() : String {
   konst v1 = A(-10.toInt()).hashCode()
   konst v2 = (-10.toInt() as Int?)!!.hashCode()
   return if( v1 == v2 ) "OK" else "$v1 $v2"
}
