internal inline konst <reified Z> Z.internalExtProp: String
    get() = "1"

private inline konst <reified Z> Z.privateExtProp: String
    get() = "2"

class Foo {
    internal inline konst <reified Z> Z.internalExtProp: String
        get() = "3"

    protected inline konst <reified Z> Z.protectedExtProp: String
        get() = "4"

    private inline konst <reified Z> Z.privateExtProp: String
        get() = "5"
}
