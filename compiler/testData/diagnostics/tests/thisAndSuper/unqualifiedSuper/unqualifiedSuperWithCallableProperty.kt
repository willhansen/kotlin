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
    <!AMBIGUOUS_SUPER!>super<!>.<!DEBUG_INFO_MISSING_UNRESOLVED!>fn<!>()

    override fun bar(): String =
            super.bar()
}
