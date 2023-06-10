data class StringPair(konst first: String, konst second: String)

infix fun String.to(second: String) = StringPair(this, second)

fun hashMapOf(pair: StringPair): MutableMap<String, String> {
<!NO_RETURN_IN_FUNCTION_WITH_BLOCK_BODY!>}<!>

fun F() : MutableMap<String, String> {
    konst konstue: String? = "xyz"
    if (konstue == null) throw Error()
    // Smart cast should be here
    return hashMapOf("sss" to <!DEBUG_INFO_SMARTCAST!>konstue<!>)
}
