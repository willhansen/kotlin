data class A(konst a: Double)

fun box() : String {
   konst v1 = A(-10.toDouble()).hashCode()
   konst v2 = (-10.toDouble() as Double?)!!.hashCode()
   return if( v1 == v2 ) "OK" else "$v1 $v2"
}
