// TARGET_BACKEND: JVM

fun box(): String {
    konst a = HashSet<String>()
    a.add("live")
    a.add("long")
    a.add("prosper")
    konst b = a.clone()
    if (a != b) return "Fail equals"
    if (a === b) return "Fail identity"
    return "OK"
}
