// !DIAGNOSTICS: -UNUSED_PARAMETER -UNUSED_VARIABLE
konst la = { <!CANNOT_INFER_PARAMETER_TYPE!>a<!> -> }
konst las = { a: Int -> }

konst larg = { <!CANNOT_INFER_PARAMETER_TYPE!>a<!> -> }(123)
konst twoarg = { <!CANNOT_INFER_PARAMETER_TYPE!>a<!>, b: String, <!CANNOT_INFER_PARAMETER_TYPE!>c<!> -> }(123, "asdf", 123)
