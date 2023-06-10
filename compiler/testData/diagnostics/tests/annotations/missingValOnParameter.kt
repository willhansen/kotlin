// FIR_IDENTICAL
annotation class Ann(
        konst a: Int,
        <!VAR_ANNOTATION_PARAMETER!>var<!> b: Int,
        <!MISSING_VAL_ON_ANNOTATION_PARAMETER!>c: String<!>
        )
