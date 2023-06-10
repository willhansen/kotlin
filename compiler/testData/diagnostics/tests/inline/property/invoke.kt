// FIR_IDENTICAL
// !DIAGNOSTICS: -UNUSED_VARIABLE -INFIX_MODIFIER_REQUIRED

inline var konstue: (p: Int) -> String
    get() = {"123" }
    set(s: (p: Int) -> String) {
        s(11)
        s.invoke(11)
        s invoke 11

        konst z = <!USAGE_IS_NOT_INLINABLE!>s<!>
    }

inline var konstue2: Int.(p: Int) -> String
    get() = {"123" }
    set(ext: Int.(p: Int) -> String) {
        11.ext(11)
        11.ext(11)

        konst p = <!USAGE_IS_NOT_INLINABLE!>ext<!>
    }