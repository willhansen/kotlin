class My {
    // No initialization needed because no backing field
    konst two: Int
        get() {
            konst <!NAME_SHADOWING!>field<!> = 2
            return field
        }
}
