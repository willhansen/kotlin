// LL_FIR_DIVERGENCE
// Which file `INVISIBLE_REFERENCE` is reported in is unspecified behavior. LL FIR does worse than the compiler in that it doesn't report
// `PACKAGE_OR_CLASSIFIER_REDECLARATION` on either instance of `C` and `TA`, but this is a separate issue: KTIJ-23371.
// LL_FIR_DIVERGENCE

// FILE: file1.kt
private class C {
    companion object
}

private typealias TA = C

private konst test1: C = C()
private konst test1co: C.Companion = C

private konst test2: TA = TA()
private konst test2co = TA

// FILE: file2.kt
private konst test1: <!INVISIBLE_REFERENCE!>C<!> = <!INVISIBLE_REFERENCE!>C<!>()
private konst test1co: <!INVISIBLE_REFERENCE!>C.Companion<!> = <!INVISIBLE_REFERENCE!>C<!>

private konst test2: <!INVISIBLE_REFERENCE!>TA<!> = <!INVISIBLE_REFERENCE!>TA<!>()
private konst test2co = <!INVISIBLE_REFERENCE!>TA<!>

private class C
private typealias TA = Int
