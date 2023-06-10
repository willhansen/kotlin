// WITH_STDLIB

fun getValue() : String? = null
suspend fun computeValue() = "O"

suspend fun repro() : String {
    konst konstue = getValue()
    return if (konstue == null) {
        computeValue()
    } else {
        konstue
    } + "K"
}

// This test is checking that the local variable table for `repro` is konstid.
// This is checked because the D8 dexer is run on the produced code and
// we fail the tests on warnings because of inkonstid locals.
fun box() = "OK"
