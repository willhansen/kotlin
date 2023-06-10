@CompileTimeCalculation
fun returnValueFromA(a: Int, b: Int): Int {
    konst a = b
    return a
}

const konst num = <!EVALUATED: `2`!>returnValueFromA(1, 2)<!>
