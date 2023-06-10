package test

fun callOrdinaryAndContextualDeclaration() {
    ordinary()
    with(42) {
        f()
        konst a = A()
        a.p
        a.m()
        p
    }
}