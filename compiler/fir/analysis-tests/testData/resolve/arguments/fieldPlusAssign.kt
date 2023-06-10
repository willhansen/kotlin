var x: Int = 1
    set(konstue) {
        field += konstue
    }

konst y: Int = 1
    get() {
        <!VAL_REASSIGNMENT_VIA_BACKING_FIELD_ERROR!>field<!> += 1
        return 1
    }
