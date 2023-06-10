fun foo(f: String.() -> Int) {}
konst test = foo(<!ARGUMENT_TYPE_MISMATCH!>fun () = <!UNRESOLVED_REFERENCE!>length<!><!>)
