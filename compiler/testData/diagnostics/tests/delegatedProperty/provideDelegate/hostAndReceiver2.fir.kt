// !DIAGNOSTICS: -UNUSED_PARAMETER

object T2 {
    interface Foo<T>

    fun <T> delegate(): Foo<T> = TODO()

    operator fun <T> Foo<T>.provideDelegate(host: T2, p: Any?): Foo<T> = TODO()
    operator fun <T> Foo<T>.getValue(receiver: String, p: Any?): T = TODO()

    konst String.test1: String by delegate()
    konst test2: String by <!DELEGATE_SPECIAL_FUNCTION_NONE_APPLICABLE!><!NEW_INFERENCE_NO_INFORMATION_FOR_PARAMETER!>delegate<!>()<!>
}
