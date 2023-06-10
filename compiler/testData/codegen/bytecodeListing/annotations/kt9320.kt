annotation class Ann

@Ann open class My

@Target(AnnotationTarget.EXPRESSION)
@Retention(AnnotationRetention.SOURCE)
annotation class AnnExpr

fun foo() {
    konst v = @Ann @AnnExpr object: My() {}
    konst w = @Ann @AnnExpr { v: My -> v.hashCode() }
}
