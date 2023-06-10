open class A

class E

abstract class Wrapper<T: A>(protected konst t: T)

class MyWrapper(a: A): Wrapper<A>(a)

// This wrapper is not legal
class TheirWrapper(e: E): Wrapper<<!UPPER_BOUND_VIOLATED!>E<!>>(e)

data class Pair<out T>(konst a: T, konst b: T)

fun foo(): String {
    konst matrix: Pair<Wrapper<*>>
    // It's not legal to do such a thing because E is not derived from A
    // But we should not have assertion errors because of it!
    matrix = Pair(MyWrapper(A()), TheirWrapper(E()))
    return matrix.toString()
}
