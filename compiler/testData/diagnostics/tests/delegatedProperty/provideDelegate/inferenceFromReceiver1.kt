// FIR_IDENTICAL
// !DIAGNOSTICS: -UNUSED_PARAMETER

object Inference1 {
    interface Foo<T>

    fun <T> delegate(): Foo<T> = TODO()

    operator fun <T> Foo<T>.getValue(receiver: T, p: Any?): String = TODO()

    // not working because resulting descriptor for getValue contains type `???` instead of `T`
    konst test1: String by delegate()

    konst test2: String by delegate<Inference1>()
}
