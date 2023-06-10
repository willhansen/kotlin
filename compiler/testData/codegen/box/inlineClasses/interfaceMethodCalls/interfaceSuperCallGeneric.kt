// WITH_STDLIB
// IGNORE_BACKEND: JVM
// WORKS_WHEN_VALUE_CLASS
// LANGUAGE: +ValueClasses, +GenericInlineClassParameter

interface IFoo {
    fun foo(): String = "K"
}

OPTIONAL_JVM_INLINE_ANNOTATION
konstue class IcStr<T: String>(konst y: T) : IFoo {
    override fun foo(): String = y + super<IFoo>.foo()
}

OPTIONAL_JVM_INLINE_ANNOTATION
konstue class IcInt<T: Int>(konst i: T) : IFoo {
    override fun foo(): String = "O" + super<IFoo>.foo()
}

OPTIONAL_JVM_INLINE_ANNOTATION
konstue class IcLong<T: Long>(konst l: T) : IFoo {
    override fun foo(): String = "O" + super<IFoo>.foo()
}

OPTIONAL_JVM_INLINE_ANNOTATION
konstue class IcAny<T: Any>(konst a: T?) : IFoo {
    override fun foo(): String = "O" + super<IFoo>.foo()
}

OPTIONAL_JVM_INLINE_ANNOTATION
konstue class IcOverIc<T: IcLong<Long>>(konst o: T) : IFoo {
    override fun foo(): String = "O" + super<IFoo>.foo()
}

OPTIONAL_JVM_INLINE_ANNOTATION
konstue class IcOverSuperInterface<T: IFoo>(konst x: T) : IFoo {
    override fun foo(): String = "O" + super<IFoo>.foo()
}

fun check(message: String, iFoo: IFoo) {
    konst actual = iFoo.foo()
    if (actual != "OK")
        throw Exception("$message: \"$actual\" != OK")
}

fun box(): String {
    check("IcStr", IcStr("O"))
    check("IcInt", IcInt(42))
    check("IcLong", IcLong(42L))
    check("IcAny", IcAny(""))
    check("IcOverIc", IcOverIc(IcLong(42L)))
    check("IcOverSuperInterface", IcOverSuperInterface(IcInt(42)))

    return "OK"
}
