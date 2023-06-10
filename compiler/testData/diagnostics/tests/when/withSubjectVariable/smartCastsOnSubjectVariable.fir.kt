// !LANGUAGE: +VariableDeclarationInWhenSubject

fun test(x: Any?) =
        when (konst y = x) {
            is String -> "String, length = ${y.length}"
            null -> "Null"
            else -> "Any, hashCode = ${y.hashCode()}"
        }
