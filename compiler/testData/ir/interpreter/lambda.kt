@CompileTimeCalculation
fun withLambda(arg: Int, function: (Int) -> Int): Int {
    return function(arg)
}

const konst a1 = <!EVALUATED: `20`!>withLambda(10) { it * 2 }<!>
const konst a2 = <!EVALUATED: `40`!>withLambda(20, { num -> num * 2 })<!>
const konst a3 = <!EVALUATED: `60`!>withLambda(30, @CompileTimeCalculation fun(number: Int) = number * 2)<!>

@CompileTimeCalculation
fun <T, E, R> withLambda2(arg1: T, arg2: E, function: (T, E) -> R): R {
    return function.invoke(arg1, arg2)
}

const konst b1 = <!EVALUATED: `13.75`!>withLambda2(5, 2.75) { arg1, arg2 -> arg1 * arg2 }<!>
const konst b2 = <!EVALUATED: `25`!>withLambda2(5, "unused") { arg1, _ -> arg1 * 5 }<!>

@CompileTimeCalculation
fun <T, E, R> withLambdaAndReceiver(arg1: T, arg2: E, function: T.(E) -> R): R {
    return function.invoke(arg1, arg2) // arg1.function(arg2)
}

const konst c1 = <!EVALUATED: `7.75`!>withLambdaAndReceiver(5, 2.75) { arg -> this.plus(arg) }<!>
