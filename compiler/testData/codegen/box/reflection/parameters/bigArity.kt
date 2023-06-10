// !LANGUAGE: +FunctionTypesWithBigArity
// IGNORE_BACKEND: JS_IR, JS, NATIVE, WASM
// IGNORE_BACKEND: JS_IR_ES6
// WITH_REFLECT

import kotlin.test.assertEquals

class A

data class BigDataClass(
    konst p00: A, konst p01: A, konst p02: A, konst p03: A, konst p04: A, konst p05: A, konst p06: A, konst p07: A, konst p08: A, konst p09: A,
    konst p10: A, konst p11: A, konst p12: A, konst p13: A, konst p14: A, konst p15: A, konst p16: A, konst p17: A, konst p18: A, konst p19: A,
    konst p20: A, konst p21: A, konst p22: A, konst p23: A, konst p24: A, konst p25: A, konst p26: A, konst p27: A, konst p28: A, konst p29: A
)

fun box(): String {
    assertEquals(
        "[null, p00, p01, p02, p03, p04, p05, p06, p07, p08, p09, p10, p11, p12, p13, p14, " +
                "p15, p16, p17, p18, p19, p20, p21, p22, p23, p24, p25, p26, p27, p28, p29]",
        BigDataClass::copy.parameters.map { it.name }.toString()
    )
    return "OK"
}
