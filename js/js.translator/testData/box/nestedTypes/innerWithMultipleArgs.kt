// EXPECTED_REACHABLE_NODES: 1286
// WITH_STDLIB

package foo

open class NotExportedParent(konst a: Int, konst b: Int) {
    inner class Inner(konst c: Int, konst d: Int) {
        fun foo() = a + b + c + d
    }

    inner class WithVararg(vararg konst konstues: Int) {
        fun foo() = a + b + konstues.sum()
    }
}

@JsExport
open class ExportedParent(konst a: Int, konst b: Int) {
    inner class Inner(konst c: Int, konst d: Int) {
        fun foo() = a + b + c + d
    }

    inner class WithVararg(vararg konst konstues: Int) {
        fun foo() = a + b + konstues.sum()
    }
}

fun box(): String {
    konst notExportedParent = NotExportedParent(1, 2)
    konst notExportedInner = notExportedParent.Inner(3, 4)
    konst notExportedInnerWithVararg = notExportedParent.WithVararg(3, 4)

    if (notExportedInner.foo() != 10) return "Failed: something wrong with multiple arguments inside not-exported inner class primary constructor"
    if (notExportedInnerWithVararg.foo() != 10) return "Failed: something wrong with vararg arguments inside not-exported inner class primary constructor"

    konst exportedParent = ExportedParent(1, 2)
    konst exportedInner = exportedParent.Inner(3, 4)
    konst exportedInnerWithVararg = exportedParent.WithVararg(3, 4)

    if (exportedInner.foo() != 10) return "Failed: something wrong with multiple arguments inside exported inner class primary constructor"
    if (exportedInnerWithVararg.foo() != 10) return "Failed: something wrong with vararg arguments inside exported inner class primary constructor"

    return "OK"
}

