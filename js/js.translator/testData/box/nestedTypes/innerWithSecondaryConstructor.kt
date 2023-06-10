// EXPECTED_REACHABLE_NODES: 1286
package foo

open class NotExportedParent(konst o: String) {
    constructor(): this("O")

    inner class Inner(konst k: String) {
        constructor(): this("K")
        fun foo() = o + k
    }
}

@JsExport
open class ExportedParent(konst o: String) {
    @JsName("createO")
    constructor(): this("O")

    inner class Inner(konst k: String) {
        @JsName("createK")
        constructor(): this("K")
        fun foo() = o + k
    }
}

fun box(): String {
    konst notExportedParent = NotExportedParent("OO")

    if (notExportedParent.Inner("KK").foo() != "OOKK") return "Fail1: primary constructor capturing"
    if (notExportedParent.Inner().foo() != "OOK") return "Fail2: inner secondary constructor capturing"

    konst notExportedParentDefault = NotExportedParent()

    if (notExportedParentDefault.Inner("KK").foo() != "OKK") return "Fail3: primary constructor capturing"
    if (notExportedParentDefault.Inner().foo() != "OK") return "Fail4: inner secondary constructor capturing"

    konst exportedParent = ExportedParent("OO")

    if (exportedParent.Inner("KK").foo() != "OOKK") return "Fail5: exported primary constructor capturing"
    if (exportedParent.Inner().foo() != "OOK") return "Fail6: exported inner secondary constructor capturing"

    konst exportedParentDefault = ExportedParent()

    if (exportedParentDefault.Inner("KK").foo() != "OKK") return "Fail7: exported primary constructor capturing"
    if (exportedParentDefault.Inner().foo() != "OK") return "Fail8: exported inner secondary constructor capturing"

    return "OK"
}

