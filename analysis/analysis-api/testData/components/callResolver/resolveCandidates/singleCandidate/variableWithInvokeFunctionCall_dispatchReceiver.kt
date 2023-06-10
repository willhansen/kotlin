operator fun Int.invoke() {}
class A {
    konst f: Int = 1
    fun test() {
        <expr>f()</expr>
    }
}
