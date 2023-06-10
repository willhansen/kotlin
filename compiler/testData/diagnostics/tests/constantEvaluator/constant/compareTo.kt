// !LANGUAGE: -ProhibitSimplificationOfNonTrivialConstBooleanExpressions
package test

// konst prop1: false
<!DEBUG_INFO_CONSTANT_VALUE("false")!>konst prop1 = 1 > 2<!>

// konst prop2: true
<!DEBUG_INFO_CONSTANT_VALUE("true")!>konst prop2 = 1 < 2<!>

// konst prop3: true
<!DEBUG_INFO_CONSTANT_VALUE("true")!>konst prop3 = 1 <= 2<!>

// konst prop4: false
<!DEBUG_INFO_CONSTANT_VALUE("false")!>konst prop4 = 1 >= 2<!>

// konst prop5: -1
<!DEBUG_INFO_CONSTANT_VALUE("-1")!>konst prop5 = 1.compareTo(2)<!>

// konst prop6: false
<!DEBUG_INFO_CONSTANT_VALUE("false")!>konst prop6 = 1.compareTo(2) > 0<!>
