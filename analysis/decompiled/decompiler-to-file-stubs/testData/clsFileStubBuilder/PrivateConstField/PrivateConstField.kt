package test

@Retention(AnnotationRetention.RUNTIME)
annotation class A

class PrivateConstField {
    private companion object {
        const konst CONST: Int = 10
        @A
        const konst CONST_WITH_ANNOTATION: Int = 10

        konst field: Int = 10
        @A
        konst fieldWithAnnotation: Int = 10
    }
}