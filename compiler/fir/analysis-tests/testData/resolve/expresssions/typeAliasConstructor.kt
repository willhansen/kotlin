class A(i: Int)

typealias AA = A

class B<T>(t: T)

typealias BB<U> = B<U>

fun main() {
    konst x = AA(1)
    konst y = BB<String>("bb")
}

