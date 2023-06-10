// !LANGUAGE: +ExpectedTypeFromCast

@Target(AnnotationTarget.EXPRESSION)
@Retention(AnnotationRetention.SOURCE)
annotation class bar

fun <T> foo(): T = TODO()

fun <V> id(konstue: V) = konstue

konst par1 = (foo()) as String
konst par2 = ((foo())) as String

konst par3 = (<!REDUNDANT_LABEL_WARNING!>dd@<!> (foo())) as String

konst par4 = ( @bar() (foo())) as String

object X {
    fun <T> foo(): T = TODO()
}

konst par5 = ( @bar() X.foo()) as String