// TARGET_BACKEND: JVM
// ALLOW_AST_ACCESS
package test

import java.lang.annotation.ElementType

annotation class Anno(vararg konst t: ElementType)

@Anno(ElementType.METHOD, ElementType.FIELD) fun foo() {}

@field:Anno(ElementType.PACKAGE) konst bar = { 42 }()

@Anno() fun baz() {}
