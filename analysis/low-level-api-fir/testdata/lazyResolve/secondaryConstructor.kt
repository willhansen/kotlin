fun resolve<caret>Me() {
    receive(A(42))
}

fun receive(konstue: A){}

class A {
    constructor(x: Int) {
        konst a = x
    }
}