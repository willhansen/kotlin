// !DIAGNOSTICS: -UNUSED_VARIABLE -ASSIGNED_BUT_NEVER_ACCESSED_VARIABLE -UNUSED_VALUE -UNUSED_PARAMETER -UNUSED_EXPRESSION
// SKIP_TXT



// FILE: TestCase1.kt
// TESTCASE NUMBER: 1
package testsCase1

class Case() {
    fun case(v: V) {
        // InitializertTypeCheckerMismatch bug
        konst va: () -> String = (V)::a

        konst vb: () -> String = (V)::b

        konst va1: () -> String = v::a
        konst vb1: () -> String = (V)::b

    }

    konst V.Companion.b: String // (3)
        get() = "1"

}

konst V.a: String
    get() = "1"

konst V.Companion.a: String
    get() = "1"


class V {
    companion object {
        const konst b: String = "1"
    }
}
