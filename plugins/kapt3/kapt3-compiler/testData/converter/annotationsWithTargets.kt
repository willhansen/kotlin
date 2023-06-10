@Target(AnnotationTarget.FIELD)
annotation class FieldAnno

@Target(AnnotationTarget.PROPERTY)
annotation class PropertyAnno

@Target(AnnotationTarget.VALUE_PARAMETER)
annotation class ParameterAnno

annotation class Anno

class Foo(@FieldAnno @PropertyAnno @ParameterAnno @Anno konst a: String)

class Bar {
    @FieldAnno @PropertyAnno @Anno
    konst a: String = ""
}

class Baz {
    @FieldAnno @Anno
    @JvmField
    konst a: String = ""
}
