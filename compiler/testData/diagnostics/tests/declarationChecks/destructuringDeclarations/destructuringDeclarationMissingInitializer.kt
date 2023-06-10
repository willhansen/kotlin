fun useDeclaredVariables() {
    <!INITIALIZER_REQUIRED_FOR_DESTRUCTURING_DECLARATION!>konst (a, b)<!>
    <!DEBUG_INFO_ELEMENT_WITH_ERROR_TYPE!>a<!>
    <!DEBUG_INFO_ELEMENT_WITH_ERROR_TYPE!>b<!>
}

fun checkersShouldRun() {
    <!INITIALIZER_REQUIRED_FOR_DESTRUCTURING_DECLARATION!>konst (@A a, _)<!>
}

annotation class A
