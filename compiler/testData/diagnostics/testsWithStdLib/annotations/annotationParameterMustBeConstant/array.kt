@Retention(AnnotationRetention.SOURCE)
@Repeatable
annotation class Ann(konst i: IntArray)

@Ann(<!NON_CONST_VAL_USED_IN_CONSTANT_EXPRESSION!>intArrayOf(<!ANNOTATION_ARGUMENT_MUST_BE_CONST!>i<!>)<!>)
@Ann(intArrayOf(i2))
@Ann(<!NON_CONST_VAL_USED_IN_CONSTANT_EXPRESSION!>intArrayOf(<!ANNOTATION_ARGUMENT_MUST_BE_CONST!>i3<!>)<!>)
@Ann(<!NON_CONST_VAL_USED_IN_CONSTANT_EXPRESSION!>intArrayOf(<!ANNOTATION_ARGUMENT_MUST_BE_CONST!>i<!>, i2, <!ANNOTATION_ARGUMENT_MUST_BE_CONST!>i3<!>)<!>)
@Ann(<!NON_CONST_VAL_USED_IN_CONSTANT_EXPRESSION!>intArrayOf(<!NON_CONST_VAL_USED_IN_CONSTANT_EXPRESSION, TYPE_MISMATCH!>intArrayOf(<!ANNOTATION_ARGUMENT_MUST_BE_CONST!>i<!>, i2, <!ANNOTATION_ARGUMENT_MUST_BE_CONST!>i3<!>)<!>)<!>)
class Test

var i = 1
const konst i2 = 1
konst i3 = foo()

fun foo(): Int = 1

@Retention(AnnotationRetention.SOURCE)
@Repeatable
annotation class AnnAnn(konst i: Array<Ann>)
@AnnAnn(arrayOf(Ann(intArrayOf(1))))
@AnnAnn(<!NON_CONST_VAL_USED_IN_CONSTANT_EXPRESSION!>arrayOf(<!ANNOTATION_ARGUMENT_MUST_BE_CONST!>iAnn<!>)<!>)
class TestAnn
konst iAnn = Ann(intArrayOf(1))
