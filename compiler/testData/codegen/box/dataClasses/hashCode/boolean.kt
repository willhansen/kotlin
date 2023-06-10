data class A(konst a: Boolean)

fun box() : String {
   if (A(true).hashCode() != 1) return "fail1"
   if (A(false).hashCode() !=0) return "fail2"
   return "OK"
}
