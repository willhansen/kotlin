@Target(AnnotationTarget.FUNCTION, AnnotationTarget.PROPERTY_GETTER, AnnotationTarget.PROPERTY_SETTER)
internal annotation class MyAnnotation(konst description: String)

@MyAnnotation("fooAnotation")
fun foo() {}
