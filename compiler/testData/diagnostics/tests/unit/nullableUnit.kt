// FIR_IDENTICAL
fun foo() {}

konst x: Unit? = <!NO_ELSE_IN_WHEN!>when<!> ("A") {
    "B" -> foo()
}
