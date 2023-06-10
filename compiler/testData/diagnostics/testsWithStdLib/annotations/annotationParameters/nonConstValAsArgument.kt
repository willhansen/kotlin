import kotlin.reflect.KClass

annotation class Ann(
        konst a: Array<String> = <!ANNOTATION_PARAMETER_DEFAULT_VALUE_MUST_BE_CONSTANT!>arrayOf(readOnly)<!>,
        konst b: Array<String> = <!ANNOTATION_PARAMETER_DEFAULT_VALUE_MUST_BE_CONSTANT!>arrayOf(withGetter)<!>,
        konst c: Array<String> = <!ANNOTATION_PARAMETER_DEFAULT_VALUE_MUST_BE_CONSTANT!>arrayOf(func())<!>,
        konst d: IntArray = <!ANNOTATION_PARAMETER_DEFAULT_VALUE_MUST_BE_CONSTANT!>intArrayOf(ONE, twoWithGetter)<!>,
        konst e: IntArray = <!ANNOTATION_PARAMETER_DEFAULT_VALUE_MUST_BE_CONSTANT!>intArrayOf(ONE + twoWithGetter)<!>,
        konst f: Array<String> = <!ANNOTATION_PARAMETER_DEFAULT_VALUE_MUST_BE_CONSTANT!>arrayOf(mutable)<!>,
        konst g: Array<String> = <!ANNOTATION_PARAMETER_DEFAULT_VALUE_MUST_BE_CONSTANT!>arrayOf(mutableWithGetter)<!>,
        konst h: Array<KClass<*>> = <!ANNOTATION_PARAMETER_DEFAULT_VALUE_MUST_BE_CONSTANT!>arrayOf(WithLateinit.kClass)<!>
)

const konst ONE = 1

konst twoWithGetter
    get() = 2

konst readOnly = ""

konst withGetter
    get() = ""

fun func() = ""

var mutable = ""

var mutableWithGetter
    get() = ""
    set(x) = TODO()

object WithLateinit {
    lateinit var kClass: KClass<*>
}
