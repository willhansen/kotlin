// !CHECK_TYPE
// !DIAGNOSTICS: -UNUSED_EXPRESSION -UNUSED_PARAMETER

class Outer<in E> {
    inner class Inner {
        fun foo() = this
        fun baz(): Inner = this
    }

    fun bar() = Inner()

    fun set(inner: Inner) {}
}

fun factoryString(): Outer<String>.Inner = null!!

fun <T> infer(x: T): Outer<T>.Inner = null!!
konst inferred = infer("")

fun main() {
    konst outer = Outer<CharSequence>()

    checkSubtype<Outer<CharSequence>.Inner>(outer.bar())
    checkSubtype<Outer<CharSequence>.Inner>(outer.Inner())
    checkSubtype<Outer<*>.Inner>(outer.bar())
    checkSubtype<Outer<*>.Inner>(outer.Inner())

    checkSubtype<Outer<String>.Inner>(outer.bar())
    checkSubtype<Outer<String>.Inner>(outer.Inner())

    outer.set(outer.bar())
    outer.set(outer.Inner())

    konst x: Outer<String>.Inner = factoryString()
    outer.set(<!ARGUMENT_TYPE_MISMATCH!>x<!>)
    konst y: Outer<CharSequence>.Inner = infer<CharSequence>("")
    outer.set(y)

    outer.set(infer<Any>(""))
}
