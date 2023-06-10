// FIR_IDENTICAL
// !DIAGNOSTICS: -UNUSED_PARAMETER -UNUSED_VARIABLE

fun println(x: String) {
}

fun run(block: () -> Unit) {}

konst propertyNameOnTheNextLine = 1

fun foo() {
    konst<!SYNTAX!><!>
    println("abc")

    konst<!SYNTAX!><!>
    run {
        println("abc")
    }

    konst<!SYNTAX!><!>
    if (1 == 1) {

    }

    konst<!SYNTAX!><!>
    (1 + 2)

    // `propertyNameOnTheNextLine` parsed as simple name expression
    konst<!SYNTAX!><!>
    propertyNameOnTheNextLine

    konst<!SYNTAX!><!>
    // comment
    propertyNameOnTheNextLine

    konst<!SYNTAX!><!> /* comment */
    propertyNameOnTheNextLine

    // Correct properties
    konst
    property1 = 1

    konst
    propertyWithBy by <!UNRESOLVED_REFERENCE!>lazy<!> { 1 }

    konst
    propertyWithType: Int

    konst
    (a, b) = <!COMPONENT_FUNCTION_MISSING, COMPONENT_FUNCTION_MISSING!>1<!>
}
