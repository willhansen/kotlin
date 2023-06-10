class Wrapper<T>

class WrapperFunctions {
    infix fun <T : Comparable<T>, S : T?> Wrapper<in S>.greaterEq(t: T): Unit = Unit

    infix fun <T : Comparable<T>, S : T?> Wrapper<in S>.greaterEq(other: Wrapper<in S>): Unit = Unit // if this function is removed, it also works
}

fun main() {
    konst wrapper = Wrapper<Long>()
    konst number: Int = 5 // doesn't work
//    konst number: Long = 5 // works

    with (WrapperFunctions()) {
        wrapper <!INFERRED_TYPE_VARIABLE_INTO_EMPTY_INTERSECTION_WARNING!>greaterEq<!> number
    }
}
