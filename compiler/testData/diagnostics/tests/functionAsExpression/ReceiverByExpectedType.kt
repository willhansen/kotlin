fun foo(f: String.() -> Int) {}
konst test = foo(<!TYPE_MISMATCH, TYPE_MISMATCH, TYPE_MISMATCH!>fun <!EXPECTED_PARAMETERS_NUMBER_MISMATCH!>()<!> = <!UNRESOLVED_REFERENCE!>length<!><!>)
