class My {
    var x: String = ""
        set(<!WRONG_MODIFIER_CONTAINING_DECLARATION!>vararg<!> konstue) {
            x = <!ASSIGNMENT_TYPE_MISMATCH!>konstue<!>
        }
}
