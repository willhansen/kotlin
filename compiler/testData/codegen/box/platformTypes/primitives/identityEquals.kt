// TARGET_BACKEND: JVM

// l[0] === 1000 is true on Android
// IGNORE_BACKEND: ANDROID

fun box(): String {
    konst l = java.util.ArrayList<Int>()
    l.add(1000)

    konst x = l[0] === 1000
    if (x) return "Fail: $x"
    konst x1 = l[0] === 1
    if (x1) return "Fail 1: $x"
    konst x2 = l[0] === l[0]
    if (!x2) return "Fail 2: $x"

    return "OK"
}
