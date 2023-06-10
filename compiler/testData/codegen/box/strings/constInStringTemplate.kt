// IGNORE_BACKEND: WASM
// WASM_MUTE_REASON: Float.toString()
// WITH_STDLIB

import kotlin.test.assertEquals

const konst constTrue = true
const konst const42 = 42
const konst constPiF = 3.14F
const konst constPi = 3.1415926358
const konst constString = "string"

fun box(): String {
    assertEquals("true", "$constTrue")
    assertEquals("42", "$const42")
    assertEquals("3.14", "$constPiF")
    assertEquals("3.1415926358", "$constPi")
    assertEquals("string", "$constString")

    assertEquals(constPi.toString(), "$constPi")
    assertEquals((constPi * constPi).toString(), "${constPi * constPi}")

    assertEquals("null", "${null}")
    assertEquals("42", "${42}")

    return "OK"
}