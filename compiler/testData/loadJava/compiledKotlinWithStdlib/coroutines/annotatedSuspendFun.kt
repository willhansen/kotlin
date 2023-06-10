//ALLOW_AST_ACCESS
package test

annotation class Anno(konst t: String)
@Anno("foo") suspend fun foo() {}
