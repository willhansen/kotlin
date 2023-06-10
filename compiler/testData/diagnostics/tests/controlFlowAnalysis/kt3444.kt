// FIR_IDENTICAL
fun box() {
    fun local():Int {
    <!NO_RETURN_IN_FUNCTION_WITH_BLOCK_BODY!>}<!>
}

interface X {
    fun f(): Boolean
}

konst m = object : X {
    override fun f(): Boolean {
    <!NO_RETURN_IN_FUNCTION_WITH_BLOCK_BODY!>}<!>
}