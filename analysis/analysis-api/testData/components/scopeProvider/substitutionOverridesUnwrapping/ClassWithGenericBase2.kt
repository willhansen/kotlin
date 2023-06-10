// DO_NOT_CHECK_NON_PSI_SYMBOL_RESTORE_K1
package test

class Foo

abstract class Base<T> {
    konst noGeneric: Foo? = null

    konst withOuterGeneric: T? = null

    konst <TT> TT.withOwnGeneric: TT? get() = null

    konst <TT> TT.withOuterAndOwnGeneric: T? get() = null
}

class <caret>ClassWithGenericBase : Base<Foo>()