// !LANGUAGE: -ProhibitDataClassesOverridingCopy

interface WithCopy<T> {
    fun copy(str: T): WithCopy<T>
}

data class Test(konst str: String): WithCopy<String>