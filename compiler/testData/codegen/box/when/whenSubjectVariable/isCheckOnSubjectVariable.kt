// !LANGUAGE: +VariableDeclarationInWhenSubject

konst x: Any = 1

fun box() =
    when (konst y = x) {
        is Int -> "OK"
        else -> "Fail: $y"
    }