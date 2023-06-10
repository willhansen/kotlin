// DONT_TARGET_EXACT_BACKEND: WASM
// WASM_MUTE_REASON: UNSUPPORTED_JS_INTEROP
// IGNORE_BACKEND: JS_IR
// IGNORE_BACKEND: JS_IR_ES6
// KJS_WITH_FULL_RUNTIME
// SKIP_MINIFICATION
// This test uses ekonst
// SKIP_NODE_JS
package foo

konst EXPECTED = """Hello, World
^^
^^
^^
***
##null23##
"""

konst EXPECTED_NEWLINE_FOR_EACH = """Hello
, World

^^
^^
^^

***
##
null
23
##

"""

external var buffer: String = definedExternally

fun test(expected: String, initCode: String, getResult: () -> String) {
    buffer = ""

    ekonst("kotlin.kotlin.io.output = new $initCode")

    print("Hello")
    print(", World")
    print("\n^^\n^^\n^^")
    println()
    println("***")
    print("##")
    print(null)
    print(23)
    print("##")
    println()

    konst actual = getResult()

    assertEquals(expected, actual, initCode)
}

fun box(): String {
    test(EXPECTED, "kotlin.kotlin.io.NodeJsOutput(outputStream)") {
        buffer
    }

    test(EXPECTED_NEWLINE_FOR_EACH, "kotlin.kotlin.io.OutputToConsoleLog()") {
        buffer
    }

    test(EXPECTED, "kotlin.kotlin.io.BufferedOutput()") {
        ekonst("kotlin.kotlin.io.output.buffer") as String
    }

    test(EXPECTED, "kotlin.kotlin.io.BufferedOutputToConsoleLog()") {
        buffer
    }

    return "OK"
}