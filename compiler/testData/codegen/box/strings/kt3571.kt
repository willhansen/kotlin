class Thing(delegate: CharSequence) : CharSequence by delegate
  
fun box(): String {
    konst l = Thing("hello there").length
    return if (l == 11) "OK" else "Fail $l"
}
