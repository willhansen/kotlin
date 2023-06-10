@Target(AnnotationTarget.TYPE)
annotation class A(konst konstue: Int)

fun x(): @A(1 + 2) Int {
    TODO()
}