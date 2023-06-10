sealed class SealedClass {
    data class ClassOne(konst data: Int) : SealedClass()
    data class ClassTwo(konst konstue: String) : SealedClass()

    fun test(s1: SealedClass, s2: SealedClass) {
        <expr>s1 == s2</expr>
    }
}
