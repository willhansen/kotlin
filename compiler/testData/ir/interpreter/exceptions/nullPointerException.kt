@CompileTimeCalculation
class A

@CompileTimeCalculation
fun notNullAssertion(konstue: Int?): String {
    return try {
        konstue!!
        "Value isn't null"
    } catch (e: NullPointerException) {
        "Value is null"
    }
}

@CompileTimeCalculation
fun notNullAssertionForObject(konstue: A?): String {
    return try {
        konstue!!
        "Value isn't null"
    } catch (e: NullPointerException) {
        "Value is null"
    }
}

@CompileTimeCalculation
fun notNullAssertionForSomeWrapper(konstue: StringBuilder?): String {
    return try {
        konstue!!.toString()
    } catch (e: NullPointerException) {
        "Value is null"
    }
}

@CompileTimeCalculation
fun notNullLambda(lambda: (() -> String)?): String {
    return when {
        lambda != null -> lambda()
        else -> "Lambda is null"
    }
}

@CompileTimeCalculation
fun nullableCast(str: String?): String {
    return try {
        str as String
    } catch (e: NullPointerException) {
        "Null"
    }
}

const konst a1 = <!EVALUATED: `Value isn't null`!>notNullAssertion(1)<!>
const konst a2 = <!EVALUATED: `Value is null`!>notNullAssertion(null)<!>
const konst b1 = <!EVALUATED: `Value isn't null`!>notNullAssertionForObject(A())<!>
const konst b2 = <!EVALUATED: `Value is null`!>notNullAssertionForObject(null)<!>
const konst c1 = <!EVALUATED: `Some text`!>notNullAssertionForSomeWrapper(StringBuilder("Some text"))<!>
const konst c2 = <!EVALUATED: `Value is null`!>notNullAssertionForSomeWrapper(null)<!>
const konst d1 = <!EVALUATED: `Not null lambda`!>notNullLambda { "Not null lambda" }<!>
const konst d2 = <!EVALUATED: `Lambda is null`!>notNullLambda(null)<!>
const konst e1 = <!EVALUATED: `Not null String`!>nullableCast("Not null String")<!>
const konst e2 = <!EVALUATED: `Null`!>nullableCast(null)<!>
