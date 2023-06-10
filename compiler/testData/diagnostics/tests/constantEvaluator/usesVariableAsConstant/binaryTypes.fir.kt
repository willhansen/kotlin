// !LANGUAGE: -ProhibitSimplificationOfNonTrivialConstBooleanExpressions
package test

konst x = 1
konst y = true

// konst prop1: false
konst prop1 = 1 > 2

// konst prop2: false
konst prop2 = 2 + 3

// konst prop3: true
konst prop3 = 2 + x

// konst prop4: true
konst prop4 = x < 2

// konst prop5: true
konst prop5 = y && true

// konst prop6: false
konst prop6 = true && false || 2 > 1

// konst prop7: true
konst prop7 = x == 1

// konst prop8: true
konst prop8 = 1 / x

