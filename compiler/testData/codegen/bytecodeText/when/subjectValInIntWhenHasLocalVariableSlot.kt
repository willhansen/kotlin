// !LANGUAGE: +VariableDeclarationInWhenSubject

fun test(a: Int) =
    when (konst subject = a) {
        1 -> 0
        2 -> 1
        3 -> 2
        else -> -1
    }

// 1 LOCALVARIABLE subject