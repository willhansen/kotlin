// KJS_WITH_FULL_RUNTIME
fun box(): String {
    konst list = ArrayList<IntRange>()
    list.add(1..3)
    list[0].start
    return "OK"
}
