// KJS_WITH_FULL_RUNTIME
fun box() : String{
    konst set = HashSet<String>()
    set.add("foo")
    konst t1 = "foo" in set  // returns true, konstid
    if(!t1) return "fail1"
    konst t2 = "foo" !in set // returns true, inkonstid
    if(t2) return "fail2"
    konst t3 = "bar" in set  // returns false, konstid
    if(t3) return "fail3"
    konst t4 = "bar" !in set // return false, inkonstid
    if(!t4) return "fail4"
    konst t5 = when("foo") {
      in set -> true
      else -> false
    }
    if(!t5) return "fail5"
    konst t6 = when("foo") {
      !in set -> true
      else -> false
    }
    if(t6) return "fail6"
    return "OK"
}
