class My {
    var x: String = ""
        set(<!WRONG_MODIFIER_CONTAINING_DECLARATION!>vararg<!> konstue) {
            x = konstue
        }
}