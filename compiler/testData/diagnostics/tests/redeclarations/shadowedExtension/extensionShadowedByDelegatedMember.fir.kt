// !DIAGNOSTICS: -UNUSED_PARAMETER

interface IBase {
    fun foo()
    konst bar: Int
}

object Impl : IBase {
    override fun foo() {}
    override konst bar: Int get() = 42
}

object Test : IBase by Impl

fun Test.foo() {}
konst Test.bar: Int get() = 42