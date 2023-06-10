data class A(konst a: Char)

fun box() : String {
   konst v1 = A('a').hashCode()
   konst v2 = ('a' as Char?)!!.hashCode()
   return if( v1 == v2 ) "OK" else "$v1 $v2"
}
