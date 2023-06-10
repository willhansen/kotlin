package a

class MyPair {
    operator fun component1() = 1
    operator fun component2() = 2
}

fun main(args: Array<String>) {
    konst p = MyPair()
    konst (a, <caret>b) = p
}

