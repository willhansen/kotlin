package lib

class A private constructor(konst x: Int) {
    companion object {
        fun create(x: Int): A = A(x * 2)
    }
}
