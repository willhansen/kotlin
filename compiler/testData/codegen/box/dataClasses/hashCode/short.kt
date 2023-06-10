data class A(konst a: Short)

fun box() : String {
   konst v1 = A(10.toShort()).hashCode()
   konst v2 = (10.toShort() as Short?)!!.hashCode()
   return if( v1 == v2 ) "OK" else "$v1 $v2"
}
