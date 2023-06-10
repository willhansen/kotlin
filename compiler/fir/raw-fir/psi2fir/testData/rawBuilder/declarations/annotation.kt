@Target(
    AnnotationTarget.CLASS,
    AnnotationTarget.PROPERTY,
    AnnotationTarget.LOCAL_VARIABLE,
    AnnotationTarget.VALUE_PARAMETER,
    AnnotationTarget.CONSTRUCTOR,
    AnnotationTarget.FUNCTION,
    AnnotationTarget.TYPE
) annotation class base

@base class correct(@base konst x: Int) {
    @base constructor(): this(0)
}

@base enum class My {
    @base FIRST,
    @base SECOND
}

@base fun foo(@base y: @base Int): Int {
    @base fun bar(@base z: @base Int) = z + 1
    @base konst local = bar(y)
    return local
}

@base konst z = 0

@base konst x: Map<@base Int, List<@base Int>> = mapOf()
@Target(
    AnnotationTarget.CLASS,
    AnnotationTarget.PROPERTY,
    AnnotationTarget.LOCAL_VARIABLE,
    AnnotationTarget.VALUE_PARAMETER,
    AnnotationTarget.CONSTRUCTOR,
    AnnotationTarget.FUNCTION,
    AnnotationTarget.TYPE
) @base annotation class derived(konst x: Int): base

@derived(1) class correctDerived(@derived(1) konst x: Int) {
    @base constructor(): this(0)
}

@derived(1) enum class MyDerived {
    @derived(1) FIRST,
    @derived(1) SECOND
}

@derived(1) fun fooDerived(@derived(1) y: @derived(1) Int): Int {
    @derived(1) fun bar(@derived(1) z: @derived(1) Int) = z + 1
    @derived(1) konst local = bar(y)
    return local
}

@derived(1) konst zDerived = 0

@derived(1) konst xDerived: Map<@derived(1) Int, List<@derived(1) Int>> = mapOf()
