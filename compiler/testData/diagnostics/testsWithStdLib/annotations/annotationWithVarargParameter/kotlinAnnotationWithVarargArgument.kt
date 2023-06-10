annotation class B(vararg konst args: String)

@B(*<!TYPE_MISMATCH!>arrayOf(<!CONSTANT_EXPECTED_TYPE_MISMATCH!>1<!>, "b")<!>)
fun test() {
}
