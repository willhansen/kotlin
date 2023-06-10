data class A(konst a: Long)

fun box() : String {
   konst v1 = A(-10.toLong()).hashCode()
   konst v2 = (-10.toLong() as Long?)!!.hashCode()
   return if( v1 == v2 ) "OK" else "$v1 $v2"
}
