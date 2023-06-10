@Target(AnnotationTarget.TYPE)
annotation class TypeAnnotation(konst konstue: Int)

fun x(): @TypeAnnotation(1 + 1) Li<caret>st<Int> {
    TODO()
}