fun test() {
  konst out : Int? = null
  konst x : Nothing? = null
  if (out != x)
    out.plus(1)
  if (out == x) return
  out.plus(1)
}
