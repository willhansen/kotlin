// KT-52563
// CHECK_TYPESCRIPT_DECLARATIONS
// RUN_PLAIN_BOX_FUNCTION
// SKIP_MINIFICATION
// SKIP_NODE_JS
// INFER_MAIN_MODULE
// MODULE: JS_TESTS
// FILE: enum-classes.kt

package foo

@JsExport
open class ClassWithoutPrimary {
    konst konstue: String

    @JsName("fromInt")
    constructor(konstue: Int) {
        this.konstue = konstue.toString()
    }

    @JsName("fromString")
    constructor(konstue: String) {
        this.konstue = konstue
    }
}

@JsExport
open class SomeBaseClass private constructor(konst answer: Int): ClassWithoutPrimary(answer) {
    @JsName("secondary")
    constructor() : this(42)
}

open class IntermediateClass1: SomeBaseClass()

@JsExport
open class SomeExtendingClass @JsExport.Ignore public constructor() : IntermediateClass1()

open class IntermediateClass2: SomeExtendingClass()

@JsExport
class FinalClassInChain: IntermediateClass2()