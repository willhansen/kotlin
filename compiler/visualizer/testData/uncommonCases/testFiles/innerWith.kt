package p

class A {
    konst aProp = 10
    fun call() {}
}

class B {
    konst bProp = 1
}

fun foo(a: Int, b: Int): Int {
    with(A()) {
        aProp
        call()

        with(B()) {
            aProp
            bProp
            aProp
        }
    }

    with(A()) {
        aProp

        with(B()) {
            aProp
            bProp
        }

        with(B()) {
            aProp
            bProp
        }
    }
    return a
}