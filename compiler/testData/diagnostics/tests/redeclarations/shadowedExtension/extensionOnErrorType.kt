// FIR_IDENTICAL

interface G<T> {
    fun foo()
    konst bar: Int
}

fun <!WRONG_NUMBER_OF_TYPE_ARGUMENTS!>G<!>.foo() {}
konst <!WRONG_NUMBER_OF_TYPE_ARGUMENTS!>G<!>.bar: Int get() = 42
