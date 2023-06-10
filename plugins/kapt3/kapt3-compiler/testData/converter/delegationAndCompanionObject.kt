// CORRECT_ERROR_TYPES

@Suppress("UNRESOLVED_REFERENCE")
interface A {
    fun inject(b: B)
    konst x: String

    companion object : B()

    abstract class B : A by Unresolved
}
