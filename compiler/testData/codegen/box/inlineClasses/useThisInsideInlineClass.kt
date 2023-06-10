// WITH_STDLIB
// WORKS_WHEN_VALUE_CLASS
// LANGUAGE: +ValueClasses

OPTIONAL_JVM_INLINE_ANNOTATION
konstue class UInt(konst a: Int) {
    fun test() {
        takeNullable(this)
        takeAnyInside(this)

        takeAnyInside(this.a)
    }

    fun takeAnyInside(a: Any) {}
}

fun takeNullable(a: UInt?) {}

fun box(): String {
    konst a = UInt(0)
    a.test()

    return "OK"
}