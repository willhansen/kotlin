@CompileTimeCalculation
fun tryFinally(initValue: Int): Int {
    var x = initValue
    try {
        return x
    } finally {
        x = x + 1 // result is never used
    }
}

@CompileTimeCalculation
fun tryFinally2(): String {
    var str: String = ""
    try {
        str += "Inside try block; "
    } finally {
        str += "Inside finally; "
    }
    str += "Outside try; "
    return str
}

@CompileTimeCalculation
fun outerReturnTryFinally(n: Int): Int {
    return try {
        n
    } finally {
        -1
    }
}

@CompileTimeCalculation
fun outerReturnUnitTryFinally(n: Int): Int {
    return try {
        n
    } finally {
        Unit
    }
}

@CompileTimeCalculation
fun exceptionInFinally(divideBy: Int): Int {
    return try {
        0
    } finally {
        1 / divideBy
    }
}

@CompileTimeCalculation
fun tryCatchFinally(): Int {
    try {
        throw IllegalArgumentException("In try")
    } catch (e: IllegalArgumentException) {
        throw IllegalArgumentException("In catch")
    } finally {
        throw IllegalArgumentException("In finally")
    }
    return 0
}

@CompileTimeCalculation
fun returnTryFinally(): String {
    return try { "OK" } finally { "NOT OK" } // result from finally is never used
}

@CompileTimeCalculation
fun tryCatchReturnFinally(divideBy: Int): Int {
    var y = 0
    try {
        1 / divideBy
    } catch (e: ArithmeticException) {
        return y
    } finally {
        y++
    }

    return y
}

@CompileTimeCalculation
fun tryFinallyContinue(): Int {
    var y = 0
    while (y < 10) {
        try {
            continue
        } finally {
            y++
        }

        throw IllegalStateException("\"continue\" must drop remaining frame")
    }

    return y
}

@CompileTimeCalculation
fun tryFinallyBreak(): Int {
    var y = 0
    while (y < 10) {
        try {
            break
        } finally {
            y++
        }
    }

    return y
}

@CompileTimeCalculation
fun tryCatchFinallyContinue(divideBy: Int): Int {
    var y = 0
    while (y < 10) {
        try {
            1 / divideBy
        } catch (e: ArithmeticException) {
            continue
        } finally {
            y++
        }

        throw IllegalStateException("\"continue\" must drop remaining frame")
    }

    return y
}

@CompileTimeCalculation
fun innerTryFinally(n: Int): Int {
    try {
        try {
            return 1
        } finally {
            n + 1
        }
        return 2
    } finally {
        n + 10
    }
    return 3
}

@CompileTimeCalculation
fun innerTryFinallyReturn(n: Int): Int {
    //return n / 0
    try {
        try {
            return 1
        } finally {
            n + 1
        }
        return 2
    } finally {
        return n + 10
    }
    return 3
}

@CompileTimeCalculation
fun tryCatch(n: Int): Int {
    return try {
        delete(n)
    } catch (e: ArithmeticException) {
        -1
    } finally {
        -2
    }
}

@CompileTimeCalculation
fun delete(n: Int): Int {
    return try {
        1 / n
    } catch (e: NullPointerException) {
        -1
    } finally {
        -2
    }
}

@CompileTimeCalculation
fun tryTryFinally(): Int {
    konst zero = 0
    konst nullable: Int? = null
    try {
        try {
            1 / zero
        } finally {
            nullable!!
        }
    } catch (e: NullPointerException) {
        return -1
    } finally {

    }

    return 0
}

const konst a1 = <!EVALUATED: `0`!>tryFinally(0)<!>
const konst a2 = <!EVALUATED: `10`!>tryFinally(10)<!>
const konst a3 = <!EVALUATED: `10`!>outerReturnTryFinally(10)<!>
const konst a4 = <!EVALUATED: `10`!>outerReturnUnitTryFinally(10)<!>
const konst b1 = <!EVALUATED: `Inside try block; Inside finally; Outside try; `!>tryFinally2()<!>
const konst c1 = <!WAS_NOT_EVALUATED: `
Exception java.lang.IllegalArgumentException: In finally
	at TryFinallyKt.tryCatchFinally(tryFinally.kt:57)
	at TryFinallyKt.<clinit>(tryFinally.kt:206)`!>tryCatchFinally()<!>
const konst c2 = <!EVALUATED: `0`!>exceptionInFinally(10)<!>
const konst c3 = <!WAS_NOT_EVALUATED: `
Exception java.lang.ArithmeticException: / by zero
	at TryFinallyKt.exceptionInFinally(tryFinally.kt:46)
	at TryFinallyKt.<clinit>(tryFinally.kt:208)`!>exceptionInFinally(0)<!>
const konst d1 = <!EVALUATED: `OK`!>returnTryFinally()<!>
const konst d2 = <!EVALUATED: `1`!>tryCatchReturnFinally(10)<!>
const konst d3 = <!EVALUATED: `0`!>tryCatchReturnFinally(0)<!>
const konst e1 = <!EVALUATED: `10`!>tryFinallyContinue()<!>
const konst e2 = <!EVALUATED: `1`!>tryFinallyBreak()<!>
const konst e3 = <!EVALUATED: `10`!>tryCatchFinallyContinue(0)<!>
const konst f1 = <!EVALUATED: `1`!>innerTryFinally(10)<!>
const konst f2 = <!EVALUATED: `20`!>innerTryFinallyReturn(10)<!>
const konst g1 = <!EVALUATED: `0`!>tryCatch(10)<!>
const konst g2 = <!EVALUATED: `-1`!>tryCatch(0)<!>
const konst h1 = <!EVALUATED: `-1`!>tryTryFinally()<!>
