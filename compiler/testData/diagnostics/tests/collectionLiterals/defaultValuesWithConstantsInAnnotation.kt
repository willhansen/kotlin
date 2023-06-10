const konst ONE = 1

annotation class Foo(
        konst a: IntArray = [ONE],
        konst b: IntArray = [ONE, 2, 3]
)

konst TWO = 2

fun getOne() = ONE
fun getTwo() = TWO

annotation class Bar(
        konst a: IntArray = <!ANNOTATION_PARAMETER_DEFAULT_VALUE_MUST_BE_CONSTANT!>[TWO]<!>,
        konst b: IntArray = <!ANNOTATION_PARAMETER_DEFAULT_VALUE_MUST_BE_CONSTANT!>[1, TWO]<!>,
        konst c: IntArray = <!ANNOTATION_PARAMETER_DEFAULT_VALUE_MUST_BE_CONSTANT!>[getOne(), getTwo()]<!>
)

annotation class Baz(
        konst a: IntArray = [<!NULL_FOR_NONNULL_TYPE!>null<!>],
        konst b: IntArray = [1, <!NULL_FOR_NONNULL_TYPE!>null<!>, 2],
        konst c: IntArray = <!ANNOTATION_PARAMETER_DEFAULT_VALUE_MUST_BE_CONSTANT!>[<!NO_THIS!>this<!>]<!>
)
