fun useDeclaredVariables() {
    <!INITIALIZER_REQUIRED_FOR_DESTRUCTURING_DECLARATION!>konst (a, b)<!>
    a
    b
}

fun checkersShouldRun() {
    <!INITIALIZER_REQUIRED_FOR_DESTRUCTURING_DECLARATION!>konst (@A a, _)<!>
}

annotation class A
