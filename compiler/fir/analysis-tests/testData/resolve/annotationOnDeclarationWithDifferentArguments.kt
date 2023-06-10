import kotlin.reflect.KClass

enum class SomeEnum {
    A, B
}

annotation class MyAnnotation(
    konst intValue: Int,
    konst stringValue: String,
    konst enumValue: SomeEnum,
    konst kClasses: Array<out KClass<*>>,
    konst annotation: MyOtherAnnotation
)
annotation class MyOtherAnnotation(konst intValue: Int, konst stringValue: String)

const konst constInt = 10
const konst constString = ""

@MyAnnotation(
    intValue = 10,
    stringValue = constString,
    enumValue = SomeEnum.A,
    kClasses = [String::class, <!ANNOTATION_ARGUMENT_MUST_BE_KCLASS_LITERAL!>constString::class<!>],

    annotation = MyOtherAnnotation(
        intValue = constInt,
        stringValue = "hello"
    )
)
fun foo() {}
