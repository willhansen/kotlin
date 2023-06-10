annotation class Ann(konst konstue: Int)

fun foo(): Int {
    konst x = 3
    @Ann(<!ANNOTATION_ARGUMENT_MUST_BE_CONST!>x<!>) konst y = 5
    return y
}
