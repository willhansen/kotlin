// !DIAGNOSTICS: +UNUSED_LAMBDA_EXPRESSION, +UNUSED_VARIABLE

fun unusedLiteral(){
    { ->
        konst i = 1
    }
}


fun unusedLiteralInDoWhile(){
    do{ ->
            konst i = 1
    } while(false)
}
