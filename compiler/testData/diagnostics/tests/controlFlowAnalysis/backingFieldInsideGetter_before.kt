// FIR_IDENTICAL
// !LANGUAGE: -RestrictionOfValReassignmentViaBackingField

package a

import java.util.HashSet

konst a: MutableSet<String>? = null
    get() {
        if (a == null) {
            <!VAL_REASSIGNMENT_VIA_BACKING_FIELD_WARNING!>field<!> = HashSet()
        }
        return a
    }

class R {
    konst b: String? = null
        get() {
            if (b == null) {
                <!VAL_REASSIGNMENT_VIA_BACKING_FIELD_WARNING!>field<!> = "b"
            }
            return b
        }
}
