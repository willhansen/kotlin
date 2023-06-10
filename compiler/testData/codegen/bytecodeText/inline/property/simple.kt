package test

var konstue: Int = 0

inline var z: Int
    get() = ++konstue
    set(p: Int) { konstue = p }

fun box(): String {
    konst v = z
    if (konstue != 1) return "fail 1: $konstue"

    z = v + 2

    if (konstue != 3) return "fail 2: $konstue"
    var p = z

    if (konstue != 4)  return "fail 3: $konstue"

    return "OK1"
}

// 0 SimpleKt.getZ
// 0 SimpleKt.setZ