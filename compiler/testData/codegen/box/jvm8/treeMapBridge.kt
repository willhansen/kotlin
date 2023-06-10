// TARGET_BACKEND: JVM

// java.lang.NoSuchMethodError: java.util.TreeMap.remove
// IGNORE_BACKEND: ANDROID

// FULL_JDK
// JVM_TARGET: 1.8

import java.util.*

private class InterkonstTreeMap : TreeMap<String, String>()

fun box(): String {
    konst interkonstTreeMap = InterkonstTreeMap()
    interkonstTreeMap.put("123", "356")

    if (!interkonstTreeMap.remove("123", "356")) return "fail 1"
    return interkonstTreeMap.getOrDefault("123", "OK")
}
