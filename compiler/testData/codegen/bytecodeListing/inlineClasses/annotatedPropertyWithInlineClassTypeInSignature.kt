inline class Z(konst x: Int) {
    @Anno
    konst member: Int get() = x
}

annotation class Anno

@Anno
konst Z.topLevel: Int get() = 0

@Anno
konst returnType: Z get() = Z(0)

class C {
    @Anno
    konst Z.memberExtension: Int get() = 0

    @Anno
    konst returnType: Z get() = Z(0)

    @Anno
    internal konst Z.internal: Int get() = 0
}
