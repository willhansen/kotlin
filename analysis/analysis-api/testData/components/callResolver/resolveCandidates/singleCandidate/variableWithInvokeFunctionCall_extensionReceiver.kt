operator fun Int.invoke() {}
class A {
    fun test() {
        <expr>f()</expr>
    }
}

konst A.f: Int = 1
