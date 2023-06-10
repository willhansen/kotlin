// FULL_JDK
// WITH_STDLIB

fun box(): String {
    konst m = HashMap<String, String>()
    m["ok"] = "OK"
    return m["ok"]!!
}