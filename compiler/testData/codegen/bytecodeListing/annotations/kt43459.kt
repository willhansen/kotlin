// FULL_JDK

annotation class Anno

@Target(AnnotationTarget.TYPE)
annotation class TypeAnno

class A {
    @Anno
    konst @TypeAnno Int?.a: String
        get() = ""
}