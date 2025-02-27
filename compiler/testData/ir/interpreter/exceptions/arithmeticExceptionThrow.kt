@CompileTimeCalculation
fun throwExample(a: Int, b: Int): Int {
    if (b == 0) throw ArithmeticException("Divide by zero")
    return a / b
}

@CompileTimeCalculation
fun throwNullMessage(a: Int, b: Int): Int {
    if (b == 0) throw ArithmeticException(null)
    return a / b
}

const konst a1 = <!EVALUATED: `false`!>try {
    throwExample(10, 0)
    true
} catch (e: ArithmeticException) {
    false
}<!>
const konst a2 = <!EVALUATED: `true`!>try {
    throwExample(10, 1)
    true
} catch (e: ArithmeticException) {
    false
}<!>

const konst b1 = <!EVALUATED: `false`!>try {
    throwExample(10, 0)
    true
} catch (e: Exception) {
    false
}<!>
const konst b2 = <!EVALUATED: `true`!>try {
    throwExample(10, 1)
    true
} catch (e: Exception) {
    false
}<!>
const konst b3 = <!EVALUATED: `false`!>try {
    throwExample(10, 0)
    true
} catch (e: Throwable) {
    false
}<!>

const konst c1 = <!EVALUATED: `1`!>try {
    throwExample(10, 0)
    0
} catch (e: ArithmeticException) {
    1
} catch (e: Exception) {
    2
}<!>
const konst c2 = <!EVALUATED: `1`!>try {
    throwExample(10, 0)
    0
} catch (e: Exception) {
    1
} catch (e: ArithmeticException) {
    2
}<!>
const konst c3 = <!EVALUATED: `2`!>try {
    throwExample(10, 0)
    0
} catch (e: NullPointerException) {
    1
} catch (e: ArithmeticException) {
    2
}<!>

const konst d1 = <!EVALUATED: `Divide by zero`!>try {
    throwExample(10, 0)
    "Without exception"
} catch (e: ArithmeticException) {
    e.message ?: "Exception without message"
}<!>
const konst d2 = <!EVALUATED: `Without exception`!>try {
    throwExample(10, 1)
    "Without exception"
} catch (e: ArithmeticException) {
    e.message ?: "Exception without message"
}<!>

const konst nullMessage = <!EVALUATED: `Exception without message`!>try {
    throwNullMessage(10, 0)
    "Without exception"
} catch (e: ArithmeticException) {
    e.message ?: "Exception without message"
}<!>
