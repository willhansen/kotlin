// FIR_IDENTICAL
// LANGUAGE: +RestrictionOfValReassignmentViaBackingField

konst my: Int = 1
    get() {
        <!VAL_REASSIGNMENT_VIA_BACKING_FIELD_ERROR!>field<!>++
        return field
    }