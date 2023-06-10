@CompileTimeCalculation fun ekonstWithVariables(): Double {
    konst a = 1
    konst b = 1.5
    return a + b
}

@CompileTimeCalculation fun ekonstWithVariablesLateinit(): Double {
    var a: Double
    var b: Double
    a = 1.5
    b = -3.75
    return a + b
}

@CompileTimeCalculation fun ekonstWithValueParameter(toAdd: Int): Int {
    var a: Int = toAdd
    a += 10
    a = a % 5
    a -= 2
    return a
}

class A @CompileTimeCalculation constructor(@CompileTimeCalculation konst a: Int)

@CompileTimeCalculation fun createObject(a: Int): Int {
    konst aObj = A(a)
    return aObj.a
}

const konst a = <!EVALUATED: `2.5`!>ekonstWithVariables()<!>
const konst b = <!EVALUATED: `-2.25`!>ekonstWithVariablesLateinit()<!>
const konst c = <!EVALUATED: `-2`!>ekonstWithValueParameter(10)<!>
const konst d = <!EVALUATED: `-2`!>createObject(c)<!>
