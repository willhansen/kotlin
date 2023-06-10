package t

class A {
    companion object B {

    }
}

konst A.B.bar : Int get() = 1

fun test() {
    <caret>A.bar
}



