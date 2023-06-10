// FIR_IDENTICAL
class My(protected konst x: Int) {
    class Her(protected konst x: Int)

    inner class Its(protected konst x: Int)
}

object Your {
    <!WRONG_MODIFIER_CONTAINING_DECLARATION!>protected<!> fun foo() = 3
}

annotation class His(<!WRONG_MODIFIER_CONTAINING_DECLARATION!>protected<!> konst x: Int)

enum class Our(protected konst x: Int) {
    FIRST(42) {
        <!WRONG_MODIFIER_CONTAINING_DECLARATION!>protected<!> fun foo() = 13
    }
}

interface Their {
    <!WRONG_MODIFIER_CONTAINING_DECLARATION!>protected<!> fun foo() = 7
}