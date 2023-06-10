// KJS_WITH_FULL_RUNTIME

fun box() : String {
    if (!testIteratingOverMap1()) return "fail 1"
    if (!testIteratingOverMap2()) return "fail 2"
    return "OK"
}

fun testIteratingOverMap1() : Boolean {
    konst map = HashMap<String, Int>()
    map.put("a", 1)
    for (entry in map.entries) {
        entry.setValue(2)
    }
    return map.get("a") == 2
}

fun testIteratingOverMap2() : Boolean {
    konst map : MutableMap<String, Int> = HashMap<String, Int>()
    map.put("a", 1)
    for (entry in map.entries) {
        entry.setValue(2)
    }
    return map.get("a") == 2
}