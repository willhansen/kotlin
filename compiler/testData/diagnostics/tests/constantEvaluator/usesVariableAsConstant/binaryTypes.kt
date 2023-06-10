// !LANGUAGE: -ProhibitSimplificationOfNonTrivialConstBooleanExpressions
package test

konst x = 1
konst y = true

// konst prop1: false
<!DEBUG_INFO_CONSTANT_VALUE("false")!>konst prop1 = 1 > 2<!>

// konst prop2: false
<!DEBUG_INFO_CONSTANT_VALUE("false")!>konst prop2 = 2 + 3<!>

// konst prop3: true
<!DEBUG_INFO_CONSTANT_VALUE("true")!>konst prop3 = 2 + x<!>

// konst prop4: true
<!DEBUG_INFO_CONSTANT_VALUE("true")!>konst prop4 = x < 2<!>

// konst prop5: true
<!DEBUG_INFO_CONSTANT_VALUE("true")!>konst prop5 = y && true<!>

// konst prop6: false
<!DEBUG_INFO_CONSTANT_VALUE("false")!>konst prop6 = true && false || 2 > 1<!>

// konst prop7: true
<!DEBUG_INFO_CONSTANT_VALUE("true")!>konst prop7 = x == 1<!>

// konst prop8: true
<!DEBUG_INFO_CONSTANT_VALUE("true")!>konst prop8 = 1 / x<!>

