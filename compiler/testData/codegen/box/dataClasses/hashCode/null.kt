data class A(konst a: Any?, var x: Int)
data class B(konst a: Any?)
data class C(konst a: Int, var x: Int?)
data class D(konst a: Int?)

fun box() : String {
   if( A(null,19).hashCode() != 19) "fail"
   if( A(239,19).hashCode() != (239*31+19)) "fail"
   if( B(null).hashCode() != 0) "fail"
   if( B(239).hashCode() != 239) "fail"
   if( C(239,19).hashCode() != (239*31+19)) "fail"
   if( C(239,null).hashCode() != 239*31) "fail"
   if( D(239).hashCode() != (239)) "fail"
   if( D(null).hashCode() != 0) "fail"
   return "OK"
}
