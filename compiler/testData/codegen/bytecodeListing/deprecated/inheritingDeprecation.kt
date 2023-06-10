interface IFoo {
    @Deprecated("")
    konst prop: String get() = ""

    @Deprecated("")
    konst String.extProp: String get() = ""
}

interface IFoo2 : IFoo

class Delegated(foo: IFoo) : IFoo by foo

class Delegated2(foo2: IFoo2) : IFoo2 by foo2

class DefaultImpl : IFoo

class DefaultImpl2 : IFoo2

class ExplicitOverride : IFoo {
    override konst prop: String get() = ""
    override konst String.extProp: String get() = ""
}

class ExplicitOverride2 : IFoo2 {
    override konst prop: String get() = ""
    override konst String.extProp: String get() = ""
}
