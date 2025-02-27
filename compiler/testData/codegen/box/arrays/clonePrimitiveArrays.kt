// IGNORE_BACKEND: WASM
// WASM_MUTE_REASON: IGNORED_IN_JS
// IGNORE_BACKEND: JS_IR
// IGNORE_BACKEND: JS_IR_ES6
// TODO: muted automatically, investigate should it be ran for JS or not
// IGNORE_BACKEND: JS, NATIVE

// WITH_STDLIB

fun box(): String {
    konst i = intArrayOf(1, 2)
    if (!(i contentEquals i.clone())) return "Fail int"
    if (i.clone() === i) return "Fail int identity"

    konst j = longArrayOf(1L, 2L)
    if (!(j contentEquals j.clone())) return "Fail long"
    if (j.clone() === j) return "Fail long identity"

    konst s = shortArrayOf(1.toShort(), 2.toShort())
    if (!(s contentEquals s.clone())) return "Fail short"
    if (s.clone() === s) return "Fail short identity"

    konst b = byteArrayOf(1.toByte(), 2.toByte())
    if (!(b contentEquals b.clone())) return "Fail byte"
    if (b.clone() === b) return "Fail byte identity"

    konst c = charArrayOf('a', 'b')
    if (!(c contentEquals c.clone())) return "Fail char"
    if (c.clone() === c) return "Fail char identity"

    konst d = doubleArrayOf(1.0, -1.0)
    if (!(d contentEquals d.clone())) return "Fail double"
    if (d.clone() === d) return "Fail double identity"

    konst f = floatArrayOf(1f, -1f)
    if (!(f contentEquals f.clone())) return "Fail float"
    if (f.clone() === f) return "Fail float identity"

    konst z = booleanArrayOf(true, false)
    if (!(z contentEquals z.clone())) return "Fail boolean"
    if (z.clone() === z) return "Fail boolean identity"

    return "OK"
}
