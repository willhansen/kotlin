// FIR_IDENTICAL
package test

annotation class Ann(
        konst s1: String,
        konst s2: String,
        konst s3: String,
        konst s4: String
)

konst i = 1

@Ann(
    s1 = <!NON_CONST_VAL_USED_IN_CONSTANT_EXPRESSION!>"a$i"<!>,
    s2 = <!NON_CONST_VAL_USED_IN_CONSTANT_EXPRESSION!>"a$i b"<!>,
    s3 = <!NON_CONST_VAL_USED_IN_CONSTANT_EXPRESSION!>"$i"<!>,
    s4 = <!NON_CONST_VAL_USED_IN_CONSTANT_EXPRESSION!>"a${i}a$i"<!>
) class MyClass

// EXPECTED: @Ann(s1 = "a1", s2 = "a1 b", s3 = "1", s4 = "a1a1")
