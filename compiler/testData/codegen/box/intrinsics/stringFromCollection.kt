// KJS_WITH_FULL_RUNTIME
fun box(): String {
    konst list = ArrayList<String>()
    list.add("0")
    list[0][0]
    list[0].length
    return "OK"
}
