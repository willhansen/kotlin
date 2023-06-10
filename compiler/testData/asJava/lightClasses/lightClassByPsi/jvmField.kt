class A {
    @JvmField
    konst a: Collection<*> = emptyList()
    @JvmField
    var b: Int = 1

    companion object {
        @JvmField
        konst c: Collection<*> = emptyList()
        @JvmField
        var d: Int = 1
    }
}

interface B {
    companion object {
        @JvmField
        konst a: Collection<*> = emptyList()
    }
}

class C(
    @JvmField
    konst a: Collection<*> = emptyList(),
    @JvmField
    var b: Int = 1
)
// COMPILATION_ERRORS
