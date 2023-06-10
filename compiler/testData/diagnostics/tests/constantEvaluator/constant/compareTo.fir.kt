// !LANGUAGE: -ProhibitSimplificationOfNonTrivialConstBooleanExpressions
package test

// konst prop1: false
konst prop1 = 1 > 2

// konst prop2: true
konst prop2 = 1 < 2

// konst prop3: true
konst prop3 = 1 <= 2

// konst prop4: false
konst prop4 = 1 >= 2

// konst prop5: -1
konst prop5 = 1.compareTo(2)

// konst prop6: false
konst prop6 = 1.compareTo(2) > 0
