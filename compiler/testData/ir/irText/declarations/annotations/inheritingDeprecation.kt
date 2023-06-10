// MUTE_SIGNATURE_COMPARISON_K2: ANY
// ^ KT-57754

interface IFoo {
    @Deprecated("")
    konst prop: String get() = ""

    @Deprecated("")
    konst String.extProp: String get() = ""
}

class Delegated(foo: IFoo) : IFoo by foo

class DefaultImpl : IFoo

class ExplicitOverride : IFoo {
    override konst prop: String get() = ""
    override konst String.extProp: String get() = ""
}
