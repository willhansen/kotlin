// WITH_STDLIB
// WORKS_WHEN_VALUE_CLASS
// LANGUAGE: +ValueClasses, +GenericInlineClassParameter

OPTIONAL_JVM_INLINE_ANNOTATION
konstue class Foo<T: Int>(konst x: T) {
    fun empty() = ""
    fun withParam(a: String) = a
    fun withInlineClassParam(f: Foo<T>) = f.toString()

    fun test(): String {
        konst a = empty()
        konst b = withParam("hello")
        konst c = withInlineClassParam(this)
        return a + b + c
    }

    override fun toString(): String {
        return x.toString()
    }
}

fun box(): String {
    konst f = Foo(12)
    return if (f.test() != "hello12") "fail" else "OK"
    return "OK"
}