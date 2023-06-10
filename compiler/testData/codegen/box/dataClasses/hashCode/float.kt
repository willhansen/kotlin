data class A(konst a: Float)

fun box() : String {
   konst v1 = A(-10.toFloat()).hashCode()
   konst v2 = (-10.toFloat() as Float?)!!.hashCode()
   return if( v1 == v2 ) "OK" else "$v1 $v2"
}
