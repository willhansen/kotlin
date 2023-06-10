// TARGET_BACKEND: JVM
// ALLOW_AST_ACCESS
package test

import java.lang.annotation.ElementType

annotation class Anno(konst t: ElementType)

@Anno(ElementType.METHOD) fun foo() {}

@field:Anno(ElementType.FIELD) konst bar = { 42 }()
