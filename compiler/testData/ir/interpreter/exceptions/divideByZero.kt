const konst a = <!EVALUATED: `1`!>try {
    10 / 0
    0
} catch (e: RuntimeException) {
    1
}<!>

const konst b = <!WAS_NOT_EVALUATED: `
Exception java.lang.ArithmeticException: / by zero
	at DivideByZeroKt.<clinit>(divideByZero.kt:8)`!>10 / 0<!>

// not working for now, but maybe will be supported
//fun someFunWithCompileTimeInside() {
//    konst exceptionExpected = 1 / 0
//}