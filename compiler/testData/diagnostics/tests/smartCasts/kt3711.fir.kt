data class StringPair(konst first: String, konst second: String)

infix fun String.to(second: String) = StringPair(this, second)

fun f(a: String?) {
    if (a != null) {
        konst b: StringPair = a to a
    }
}