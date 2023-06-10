// !LANGUAGE: +VariableDeclarationInWhenSubject

konst x = 1

fun box() =
    when (konst y = x) {
        in 0..2 -> "OK"
        else -> "Fail: $y"
    }