// !DIAGNOSTICS: -UNUSED_VARIABLE -UNUSED_PARAMETER -TOPLEVEL_TYPEALIASES_ONLY
// NI_EXPECTED_FILE

class C {
    typealias Self = C
    class Nested {
        class N2
        typealias Root = C
    }
    companion object X {
        konst ok = "OK"
        class InCompanion
    }
}

konst c = C.Self.<!UNRESOLVED_REFERENCE!>Self<!>()
konst n = C.Self.<!UNRESOLVED_REFERENCE!>Nested<!>()
konst x = C.Self.<!UNRESOLVED_REFERENCE!>X<!>
konst n2 = C.Nested.Root.<!UNRESOLVED_REFERENCE!>Nested<!>.N2()
konst ic = C.Self.<!UNRESOLVED_REFERENCE!>InCompanion<!>()
konst ok = C.Self.ok
