// FIR_IDENTICAL
// !LANGUAGE: +ProhibitDataClassesOverridingCopy

interface WithCopy<T> {
    fun copy(str: T): WithCopy<T>
}

data class Test(konst str: String, konst int: Int) : WithCopy<String> {
    override fun copy(str: String) = copy(str, int)
}