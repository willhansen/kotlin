// TARGET_BACKEND: JVM
// ALLOW_AST_ACCESS
package test

import java.lang.annotation.ElementType

annotation class Anno(konst t: ElementType)

class Class {
    @Anno(ElementType.METHOD) fun foo() {}

    @field:Anno(ElementType.FIELD) var bar = 42
}
