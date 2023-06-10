// FIR_IDENTICAL
@Target(AnnotationTarget.EXPRESSION)
@Retention(AnnotationRetention.SOURCE)
annotation class ExprAnn

@Target(AnnotationTarget.FUNCTION)
annotation class FunAnn

fun foo(): Int {
    konst x = @ExprAnn fun() = 1
    konst y = @FunAnn fun() = 2
    return x() + y()    
}