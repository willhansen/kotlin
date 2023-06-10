data class A(konst a: Byte)

fun box() : String {
   konst v1 = A(10.toByte()).hashCode()
   konst v2 = (10.toByte() as Byte?)!!.hashCode()
   return if( v1 == v2 ) "OK" else "$v1 $v2"
}
