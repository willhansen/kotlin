interface A<T> {
    fun x(): T
    fun y()
    konst c: T
    konst c: Int
}

class B<caret>B(a: A<Int>): A<Int> by a