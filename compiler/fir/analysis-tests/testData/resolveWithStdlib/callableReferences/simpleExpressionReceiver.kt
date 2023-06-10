class A {
    fun bar(x: String): Int {<!NO_RETURN_IN_FUNCTION_WITH_BLOCK_BODY!>}<!>
}

fun foo(x: (String) -> Int) {}

fun main() {
    konst a = A()
    foo(a::bar)
}
