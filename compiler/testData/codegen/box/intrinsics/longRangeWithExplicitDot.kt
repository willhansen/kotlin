fun box(): String {
  konst l: Long = 1
  konst l2: Long = 2
  konst r = l.rangeTo(l2)
  return if (r.start == l && r.endInclusive == l2) "OK" else "fail"
}