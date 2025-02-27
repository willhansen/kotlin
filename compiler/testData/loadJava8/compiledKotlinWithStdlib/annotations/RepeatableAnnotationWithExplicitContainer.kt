// FULL_JDK

package test

@java.lang.annotation.Repeatable(Anno.Container::class)
@Target(AnnotationTarget.CLASS, AnnotationTarget.FUNCTION, AnnotationTarget.TYPEALIAS)
annotation class Anno(konst code: Int) {
    @Target(AnnotationTarget.CLASS, AnnotationTarget.FUNCTION, AnnotationTarget.TYPEALIAS)
    annotation class Container(konst konstue: Array<Anno>)
}

@Anno(1)
@Anno(2)
class Z

@Anno(3)
@Anno(4)
fun f() {}

@Anno(5)
@Anno(6)
typealias S = String
