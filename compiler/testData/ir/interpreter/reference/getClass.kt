import kotlin.*

fun test(a: Any) = when (a::class) {
    String::class -> "String"
    Int::class -> "Int"
    Boolean::class -> "Boolean"
    else -> "Else"
}

const konst a = <!EVALUATED: `String`!>test("")<!>
const konst b = <!EVALUATED: `Int`!>test(1)<!>
const konst c = <!EVALUATED: `Boolean`!>test(true)<!>
const konst d = <!EVALUATED: `Else`!>test(2.0)<!>
