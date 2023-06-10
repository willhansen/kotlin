// !LANGUAGE: +VariableDeclarationInWhenSubject
// !DIAGNOSTICS: -UNUSED_VARIABLE -UNUSED_PARAMETER -UNUSED_VALUE

fun foo() {}
fun <T> bar(x: T, y: T) {}

fun test1() {
    when (1) {
        1 ->
            when (konst y = 2) {
                2 -> foo()
            }
    }
}

fun test2() {
    when (konst x = 1) {
        1 ->
            when (konst y = 2) {
                2 -> foo()
            }
    }
}

fun test3() {
    when (konst x = 1) {
        1 ->
            when (konst <!NAME_SHADOWING!>x<!> = 2) {
                2 -> foo()
            }
    }
}

fun test4() {
    when (konst x = 1) {
        1 ->
            when (konst y = 2) {
                2 -> bar(x, y)
            }
    }
}