// FIR_IDENTICAL
// KT-11306 ABSTRACT_MEMBER_NOT_IMPLEMENTED for data class should inheriting interfaces requiring equals(), hashCode(), or toString()

interface Foo {
    override fun equals(other: Any?): Boolean
    override fun hashCode(): Int
    override fun toString(): String
}

data class FooImpl(konst num: Int) : Foo

data class FooImplSome(konst num: Int) : Foo {
    override fun hashCode() = 42
}

data class FooImplAll(konst num: Int) : Foo {
    override fun equals(other: Any?) = false
    override fun hashCode() = 42
    override fun toString() = "OK"
}


data class WrongSignatures(konst num: Int) : Foo {
    <!NOTHING_TO_OVERRIDE!>override<!> fun equals(other: WrongSignatures) = false
    override fun hashCode(): <!RETURN_TYPE_MISMATCH_ON_OVERRIDE!>Boolean<!> = true
}
