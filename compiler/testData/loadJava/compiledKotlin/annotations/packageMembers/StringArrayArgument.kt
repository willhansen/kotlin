// PLATFORM_DEPENDANT_METADATA
//ALLOW_AST_ACCESS
package test

annotation class Anno(vararg konst t: String)

@Anno("live", "long") fun foo() {}

@field:Anno("prosper") konst bar = { 42 }()

@Anno() fun baz() {}
