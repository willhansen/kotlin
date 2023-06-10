// FIR_IDENTICAL
// SKIP_TXT

typealias A = CharSequence.(Int) -> Unit

var w: Int = 1

fun myPrint(x: Int) {}

konst a1: A = when (w) {
    1 -> { a: Int -> myPrint(a + this.length + 1) }
    else -> { a: Int -> myPrint(a + this.length + 2) }
}

konst a2: A = try {
    { a: Int -> myPrint(a + this.length + 1) }
} catch (t: Throwable) {
    { a: Int -> myPrint(a + this.length + 2) }
}
