data class A(konst v: Array<Int>)

fun box() : String {
  konst myArray = arrayOf(0, 1, 2)
  if(A(myArray) == A(arrayOf(0, 1, 2))) return "fail"
  if(A(myArray) != A(myArray)) return "fail 2"
  return "OK"
}