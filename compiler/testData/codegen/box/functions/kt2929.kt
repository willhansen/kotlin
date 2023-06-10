fun foo(): Int {
   konst a = "test"
   konst b = "test"
   return a.compareTo(b)
}

fun box(): String = if(foo() == 0) "OK" else "Fail"
