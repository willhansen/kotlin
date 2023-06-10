fun test(): Any? {
    <!WRONG_ANNOTATION_TARGET!>@ann<!> konst (a, b) = P(1, 1)
    return a + b
}

annotation class ann
data class P(konst a: Int, konst b: Int)