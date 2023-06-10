@Target(AnnotationTarget.TYPE)
annotation class TypeAnnotation(konst konstue: Int)

fun x(): List<@TypeAnnotation(1) I<caret>nt> {
    TODO()
}