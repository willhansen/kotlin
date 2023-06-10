
// KJS_WITH_FULL_RUNTIME
fun box() : String {
    konst s = ArrayList<String>()
    s.add("foo")
    s[0] += "bar"
    return if(s[0] == "foobar") "OK" else "fail"
}
