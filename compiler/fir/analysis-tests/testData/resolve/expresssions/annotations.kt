// WITH_STDLIB

@Retention(AnnotationRetention.SOURCE)
@Target(AnnotationTarget.EXPRESSION)
annotation class MyAnn

fun bar(x: Int) {}

fun foo() {
    <!WRONG_ANNOTATION_TARGET!>@MyAnn<!>
    konst x: Int
    @MyAnn
    x = @MyAnn 42
    @MyAnn
    bar(@MyAnn x)

    konst y = @MyAnn if (false) x else x
    konst z = @MyAnn try {
        x
    } catch (t: Throwable) {
        0
    }
}
