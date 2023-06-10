interface A
class B: A

konst X = B()

fun foo(b: B) {}

fun main(a: A) {
    if (a === X) {
        foo(a)
    }
}
