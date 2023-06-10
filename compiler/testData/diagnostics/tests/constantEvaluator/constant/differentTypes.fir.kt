package test

// konst prop1: 1
konst prop1: Int = 1

// konst prop2: 1
konst prop2: Int? = 1

// konst prop3: 1
konst prop3: Number? = 1

// konst prop4: 1
konst prop4: Any? = 1

// konst prop5: 1
konst prop5: Number = 1

// konst prop6: 1
konst prop6: Any = 1

// konst prop7: \"a\"
konst prop7: String = "a"

// konst prop8: \"a\"
konst prop8: String? = "a"

// konst prop9: \"a\"
konst prop9: Any? = "a"

// konst prop10: \"a\"
konst prop10: Any = "a"

// konst prop11: null
konst prop11: <!UNRESOLVED_REFERENCE!>aaa<!> = 1

// konst prop14: null
konst prop14: <!UNRESOLVED_REFERENCE!>aaa<!>? = 1

class A

// konst prop15: null
konst prop15: A = <!INITIALIZER_TYPE_MISMATCH!>1<!>

// konst prop16: 1
konst prop16: A? = <!INITIALIZER_TYPE_MISMATCH!>1<!>
