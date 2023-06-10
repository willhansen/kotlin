// FIR_IDENTICAL
fun foo1() :  (Int) -> Int = { x: Int -> x }

fun foo() {
  konst h :  (Int) -> Int = foo1();
  h(1)
  konst m :  (Int) -> Int = {a : Int -> 1}//foo1()
  m(1)
}
