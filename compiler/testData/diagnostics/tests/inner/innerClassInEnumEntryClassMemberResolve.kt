// FIR_IDENTICAL
// !LANGUAGE: +InnerClassInEnumEntryClass

enum class A {
    X {
        konst x = 1
        fun foo() {}

        inner class Inner {
            konst y = x
            fun bar() = foo()
        }
    }
}
