// !LANGUAGE: +VariableDeclarationInWhenSubject

fun test(a: String) =
    when (konst subject = a) {
        "" -> 0
        "a" -> 1
        else -> -1
    }

// 1 LOCALVARIABLE subject