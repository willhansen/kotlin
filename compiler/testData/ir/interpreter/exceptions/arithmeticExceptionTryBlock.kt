@CompileTimeCalculation
fun tryCatch(integer: Int): Boolean {
    try {
        konst a = 10 / integer
        return true
    } catch (e: ArithmeticException) {
        return false
    }
}

const konst a1 = <!EVALUATED: `false`!>tryCatch(0)<!>
const konst a2 = <!EVALUATED: `true`!>tryCatch(1)<!>
const konst a3 = <!EVALUATED: `true`!>tryCatch(100)<!>

@CompileTimeCalculation
fun multiTryCatch(integer: Int): String {
    return try {
        konst a = 10 / integer
        "Normal"
    } catch (e: AssertionError) {
        "AssertionError"
    } catch (e: ArithmeticException) {
        "ArithmeticException"
    }
}

const konst b1 = <!EVALUATED: `ArithmeticException`!>multiTryCatch(0)<!>
const konst b2 = <!EVALUATED: `Normal`!>multiTryCatch(1)<!>
