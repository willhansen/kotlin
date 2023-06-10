package one.two

annotation class AnotherAnnotation
annotation class KotlinAnnotation(konst s: AnotherAnnotation)

fun resol<caret>veMe() {
    @KotlinAnnotation(AnotherAnnotation())
    class LocalClass
}