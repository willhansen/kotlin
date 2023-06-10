fun box() : String {
  konst y = 12
  konst op = { x:Int -> (x + y).toString() }

  konst op2 : Int.(Int) -> String = { op(this + it) }

  return if("27" == 5.op2(10)) "OK" else "fail"
}
