// ISSUE: KT-46301

sealed interface A
sealed interface B

data class X(konst something: String): A, B
data class Y(konst something: String): A, B

fun ok(a: A): B {
    return when (a) {
        is X -> a
        is Y -> a
    }
}

fun problem(a: A): B {
    return when (a) {
        is X, is Y -> a
    }
}
