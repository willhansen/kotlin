// FIR_IDENTICAL
// DIAGNOSTICS: -DEBUG_INFO_LEAKING_THIS
class Foo : I {
    // no getter
    konst final_notInitializedInPlace_deferredInit0: Int
    <!MUST_BE_INITIALIZED_OR_BE_ABSTRACT!>konst final_notInitializedInPlace0: Int<!>
    konst final_initializedInPlace0: Int = 1
    override konst open_notInitializedInPlace_deferredInit0: Int
    <!MUST_BE_INITIALIZED_OR_BE_ABSTRACT!>override konst open_notInitializedInPlace0: Int<!>
    override konst open_initializedInPlace0: Int = 1

    // getter with field
    konst final_notInitializedInPlace_deferredInit1: Int; get() = field
    <!MUST_BE_INITIALIZED!>konst final_notInitializedInPlace1: Int<!>; get() = field
    konst final_initializedInPlace1: Int = 1; get() = field
    override konst open_notInitializedInPlace_deferredinit1: Int; get() = field
    <!MUST_BE_INITIALIZED!>override konst open_notInitializedInPlace1: Int<!>; get() = field
    override konst open_initializedInPlace1: Int = 1; get() = field

    // getter with empty body
    konst final_notInitializedInPlace_deferredInit2: Int; get
    <!MUST_BE_INITIALIZED_OR_BE_ABSTRACT!>konst final_notInitializedInPlace2: Int<!>; get
    konst final_initializedInPlace2: Int = 1; get
    override konst open_notInitializedInPlace_deferredinit2: Int; get
    <!MUST_BE_INITIALIZED_OR_BE_ABSTRACT!>override konst open_notInitializedInPlace2: Int<!>; get
    override konst open_initializedInPlace2: Int = 1; get

    // getter no field
    konst final_notInitializedInPlace_deferredInit3: Int; get() = 1
    konst final_notInitializedInPlace3: Int; get() = 1
    konst final_initializedInPlace3: Int = <!PROPERTY_INITIALIZER_NO_BACKING_FIELD!>1<!>; get() = 1
    override konst open_notInitializedInPlace_deferredinit3: Int; get() = 1
    override konst open_notInitializedInPlace3: Int; get() = 1
    override konst open_initializedInPlace3: Int = <!PROPERTY_INITIALIZER_NO_BACKING_FIELD!>1<!>; get() = 1

    init {
        final_notInitializedInPlace_deferredInit0 = 1
        final_notInitializedInPlace_deferredInit1 = 1
        final_notInitializedInPlace_deferredInit2 = 1
        <!VAL_REASSIGNMENT!>final_notInitializedInPlace_deferredInit3<!> = 1

        open_notInitializedInPlace_deferredInit0 = 1
        open_notInitializedInPlace_deferredinit1 = 1
        open_notInitializedInPlace_deferredinit2 = 1
        <!VAL_REASSIGNMENT!>open_notInitializedInPlace_deferredinit3<!> = 1
    }
}

interface I {
    konst open_notInitializedInPlace_deferredInit0: Int
    konst open_notInitializedInPlace_deferredinit1: Int
    konst open_notInitializedInPlace_deferredinit2: Int
    konst open_notInitializedInPlace_deferredinit3: Int

    konst open_notInitializedInPlace0: Int
    konst open_notInitializedInPlace1: Int
    konst open_notInitializedInPlace2: Int
    konst open_notInitializedInPlace3: Int

    konst open_initializedInPlace0: Int
    konst open_initializedInPlace1: Int
    konst open_initializedInPlace2: Int
    konst open_initializedInPlace3: Int
}
