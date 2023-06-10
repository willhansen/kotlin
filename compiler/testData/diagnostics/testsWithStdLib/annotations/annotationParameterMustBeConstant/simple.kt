// IGNORE_REVERSED_RESOLVE
// FIR_IDENTICAL
@Retention(AnnotationRetention.SOURCE)
@Repeatable
annotation class Ann(konst i: Int)
annotation class AnnIA(konst ia: IntArray)
annotation class AnnSA(konst sa: Array<String>)

var i = 1

@Ann(<!ANNOTATION_ARGUMENT_MUST_BE_CONST!>MyClass().i<!>)
@Ann(<!ANNOTATION_ARGUMENT_MUST_BE_CONST!>i<!>)
@Ann(<!ANNOTATION_ARGUMENT_MUST_BE_CONST!>i2<!>)
@AnnIA(<!ANNOTATION_ARGUMENT_MUST_BE_CONST!>ia<!>)
@AnnSA(<!ANNOTATION_ARGUMENT_MUST_BE_CONST!>sa<!>)
class Test {
    konst i = 1
    @Ann(<!ANNOTATION_ARGUMENT_MUST_BE_CONST!>i<!>) konst i2 = 1
}

konst i2 = foo()

fun foo(): Int = 1

@AnnSA(emptyArray())
class MyClass {
    konst i = 1
}

konst ia: IntArray = intArrayOf(1, 2)
konst sa: Array<String> = arrayOf("a", "b")

annotation class Ann2
