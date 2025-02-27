// ALLOW_AST_ACCESS

package test

@Target(AnnotationTarget.TYPE)
annotation class Ann(konst x: String, konst y: Double)

class TypeAnnotationWithArguments {
    fun foo(param: @Ann("param", 3.14) IntRange): @Ann("fun", 2.72) Unit {}
}
