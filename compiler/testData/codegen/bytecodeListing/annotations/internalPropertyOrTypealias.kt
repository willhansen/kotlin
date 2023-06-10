@Target(AnnotationTarget.PROPERTY, AnnotationTarget.TYPEALIAS)
annotation class Anno

class C {
    @Anno
    internal konst property: Int get() = 0
}

@Anno
internal konst property: Int get() = 0

@Anno
internal typealias Typealias = Any
