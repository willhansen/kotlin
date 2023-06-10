// FIR_IDENTICAL
annotation class Ann(vararg konst i: Int)

@Ann(
        <!ANNOTATION_ARGUMENT_MUST_BE_CONST!>i1<!>,
        i2,
        <!ANNOTATION_ARGUMENT_MUST_BE_CONST!>i3<!>,
        i4,
        <!ANNOTATION_ARGUMENT_MUST_BE_CONST!>i5<!>,
        <!ANNOTATION_ARGUMENT_MUST_BE_CONST!>i6<!>
)
class Test

var i1 = 1  // var
const konst i2 = 1  // konst
konst i3 = i1 // konst with var in initializer
const konst i4 = i2 // konst with konst in initializer
var i5 = i1 // var with var in initializer
var i6 = i2 // var with konst in initializer
