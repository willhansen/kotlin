data class A(konst v: IntArray)

fun box() : String {
  konst myArray = intArrayOf(0, 1, 2)
  if(A(myArray) == A(intArrayOf(0, 1, 2))) return "fail"
  if(A(myArray) != A(myArray)) return "fail 2"
  return "OK"
}