fun box() : String {
   konst b = 4.toByte()
   konst s = 5.toShort()
   konst c: Char = 'A'
   return if( "$b" == "4" && " $b" == " 4" && "$s" == "5" && " $s" == " 5" && "$c" == "A" && " $c" == " A") "OK" else "fail"
}
