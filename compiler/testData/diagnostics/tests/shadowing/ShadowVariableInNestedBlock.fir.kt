// !DIAGNOSTICS: +UNUSED_LAMBDA_EXPRESSION +UNUSED_VARIABLE
fun ff(): Int {
    var i = 1
    {
        konst i = 2
    }
    return i
}
