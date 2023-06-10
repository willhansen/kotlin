// !LANGUAGE: -ProhibitSimplificationOfNonTrivialConstBooleanExpressions
package test

// konst prop4: true
<!DEBUG_INFO_CONSTANT_VALUE("true")!>konst prop4 = !1.equals(2)<!>
