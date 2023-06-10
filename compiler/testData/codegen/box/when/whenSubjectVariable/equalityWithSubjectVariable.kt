// !LANGUAGE: +VariableDeclarationInWhenSubject

konst x = 1

fun box() =
    when (konst y = x) {
        1 -> "OK"
        else -> "Fail: $y"
    }