import kotlin.reflect.KClass

open class A
class B1 : A()
class B2 : A()

annotation class Ann1(konst arg: KClass<in A>)

@Ann1(A::class)
class MyClass1

@Ann1(Any::class)
class MyClass1a

@Ann1(<!ARGUMENT_TYPE_MISMATCH!>B1::class<!>)
class MyClass2

annotation class Ann2(konst arg: KClass<in B1>)

@Ann2(A::class)
class MyClass3

@Ann2(B1::class)
class MyClass4

@Ann2(<!ARGUMENT_TYPE_MISMATCH!>B2::class<!>)
class MyClass5
