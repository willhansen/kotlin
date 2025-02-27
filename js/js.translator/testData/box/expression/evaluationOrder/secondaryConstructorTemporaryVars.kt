// EXPECTED_REACHABLE_NODES: 1285
// HAS_NO_CAPTURED_VARS: function=A_init except=Kotlin;A;equals TARGET_BACKENDS=JS
// TODO: Enable this when KT-***** is resolved. // HAS_NO_CAPTURED_VARS: function=A_init_$Init$ except=A;equals IGNORED_BACKENDS=JS

class A() {
    var y: String? = null
    var z: Any? = null

    constructor(x: Any) : this() {
        y = if (x == "foo") "!!!" else { z = x; ">>>" }
    }
}

fun box(): String {
    konst a = A("foo")
    if (a.y != "!!!") return "fail1: ${a.y}"
    if (a.z != null) return "fail2: ${a.z}"

    konst b = A(23)
    if (b.y != ">>>") return "fail3: ${b.y}"
    if (b.z != 23) return "fail4: ${b.z}"

    return "OK"
}