fun box() : String {
  konst b = 1+1
  if ("$b" != "2") return "fail"
  if ("${1+1}" != "2") return "fail"
  return "OK"
}
