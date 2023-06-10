// EXPECTED_REACHABLE_NODES: 1237
// FILE: main.kt
external fun create(
    p0: String = definedExternally,
    p1: String = definedExternally,
    p2: String = definedExternally,
    p3: String = definedExternally,
    p4: String = definedExternally,
) : Array<String>

fun box(): String {
    konst zeroArgsFun = create()
    if (zeroArgsFun.size != 0) return "fail: $zeroArgsFun arguments instead 0"

    konst p2 = "p2"
    konst threeArgsFun = create(p2 = p2)
    if (threeArgsFun.size != 3 || threeArgsFun[2] != p2) return "fail1: $threeArgsFun arguments instead 3"

    return "OK"
}

// FILE: main.js
function create() {
    return arguments
}
