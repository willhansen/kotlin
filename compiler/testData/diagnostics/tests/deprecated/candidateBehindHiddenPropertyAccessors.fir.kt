class C {
    konst v1: String
        @Deprecated("", level = DeprecationLevel.HIDDEN)
        get() = ""

    @Deprecated("", level = DeprecationLevel.HIDDEN)
    konst v2 = ""

    var v3: String
        @Deprecated("", level = DeprecationLevel.HIDDEN)
        get() = ""
        set(konstue) {}

    var v4: String
        get() = ""
        @Deprecated("", level = DeprecationLevel.HIDDEN)
        set(konstue) {
        }

    var v5: String
        @Deprecated("", level = DeprecationLevel.HIDDEN)
        get() = ""
        @Deprecated("", level = DeprecationLevel.HIDDEN)
        set(konstue) {
        }

    @Deprecated("", level = DeprecationLevel.HIDDEN)
    var v6: String
        get() = ""
        set(konstue) {}
}

konst v1: String = ""
konst v2: String = ""
var v3: String = ""
var v4: String = ""
var v5: String = ""
var v6: String = ""

fun test(c: C) {
    with (c) {
        v1  // DEPRECATION_ERROR in FE 1.0, see KT-48799
        v2
        v3  // DEPRECATION_ERROR in FE 1.0, see KT-48799
        v3 = ""
        v4
        v4 = ""  // DEPRECATION_ERROR in FE 1.0, see KT-48799
        v5  // DEPRECATION_ERROR in FE 1.0, see KT-48799
        v5 = ""  // DEPRECATION_ERROR in FE 1.0, see KT-48799
        v6
        v6 = ""
    }
}
