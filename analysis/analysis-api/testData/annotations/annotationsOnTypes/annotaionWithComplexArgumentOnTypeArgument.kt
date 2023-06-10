@Target(AnnotationTarget.TYPE)
annotation class TypeAnnotation(konst konstue: Int)

fun x(): MutableList<@TypeAnnotation(1 + 1) L<caret>ist<Int>> {
    TODO()
}