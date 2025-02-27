// Ambiguity between fun and callable property

open class BaseWithCallableProp {
    konst fn = { "fn.invoke()" }

    konst bar = { "bar.invoke()"}
    open fun bar(): String = "bar()"
}

interface InterfaceWithFun {
    fun fn(): String = "fn()"
}

class DerivedUsingFun : BaseWithCallableProp(), InterfaceWithFun {
    fun foo(): String =
    <!AMBIGUOUS_SUPER!>super<!>.fn()

    override fun bar(): String =
            super.bar()
}
