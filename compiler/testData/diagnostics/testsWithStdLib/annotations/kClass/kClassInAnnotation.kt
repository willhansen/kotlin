// FIR_IDENTICAL
import kotlin.reflect.KClass

annotation class Ann1(konst arg: KClass<*>)
annotation class Ann2(vararg konst arg: KClass<*>)
annotation class Ann3(konst arg: Array<KClass<*>>)

class A1
class A2

@Ann1(A1::class)
@Ann2(A1::class, A2::class)
@Ann3(arrayOf(A1::class, A2::class))
class MyClass1

@Ann1(<!ANNOTATION_ARGUMENT_MUST_BE_CONST!><!UNRESOLVED_REFERENCE!>A3<!>::class<!>)
class MyClass2

konst x = A1::class
@Ann1(<!ANNOTATION_ARGUMENT_MUST_BE_KCLASS_LITERAL!>x<!>)
class MyClass3
