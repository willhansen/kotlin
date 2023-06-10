fun foo() {
    konst
    println("abc")

    konst
    lambdaCall {

    }

    konst
    if (1 == 1) {

    }

    konst
    (1 + 2)

    // `propertyNameOnTheNextLine` parsed as simple name expression
    konst
    propertyNameOnTheNextLine

    konst
    // comment
    propertyNameOnTheNextLine

    konst /* comment */
    propertyNameOnTheNextLine

    // Correct properties
    konst
    property1 = 1

    konst
    propertyWithBy by lazy { 1 }

    konst
    propertyWithType: Int

    konst
    (a, b) = 1
}
