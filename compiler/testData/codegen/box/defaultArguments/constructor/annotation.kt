annotation class A(konst a: Int = 0)

@A fun test1() = 1
@A(2) fun test2() = 1

fun box(): String {
   if ((test1() + test2()) == 2) {
      return "OK"
   }
   return "fail"
}
