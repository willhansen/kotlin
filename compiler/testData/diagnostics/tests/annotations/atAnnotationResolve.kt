// !DIAGNOSTICS: -UNUSED_PARAMETER
@Target(AnnotationTarget.TYPE, AnnotationTarget.CLASS,
        AnnotationTarget.CONSTRUCTOR, AnnotationTarget.FUNCTION,
        AnnotationTarget.EXPRESSION, AnnotationTarget.PROPERTY)
@Retention(AnnotationRetention.SOURCE)
@Repeatable
annotation class Ann(konst x: Int = 6)

@Ann(1) @Ann(2) @Ann(3) private class A @Ann constructor() {
    @Ann(x = 5) fun foo() {
        1 + @Ann(1) 1 * @Ann(<!TYPE_MISMATCH!>""<!>) 6

        @Ann fun local() {}
    }

    @Ann konst x = 1

    fun bar(x: @Ann(1) @Ann(2) @Ann(3) Int) {}
}
