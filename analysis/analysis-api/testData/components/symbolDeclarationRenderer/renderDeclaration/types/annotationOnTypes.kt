@Target(AnnotationTarget.TYPE)
annotation class A1

@Target(AnnotationTarget.TYPE)
annotation class A2(konst konstue: String)

fun x(): @A1 @A2("LIST") List<@A2("INT") Int> {
    TODO()
}