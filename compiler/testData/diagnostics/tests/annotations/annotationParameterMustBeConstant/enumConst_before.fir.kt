// !LANGUAGE: -ProhibitNonConstValuesAsVarargsInAnnotations

annotation class AnnE(konst i: MyEnum)

@AnnE(<!ANNOTATION_ARGUMENT_MUST_BE_ENUM_CONST!>e<!>)
class Test

konst e: MyEnum = MyEnum.A

enum class MyEnum {
    A
}

@AnnE(<!ANNOTATION_ARGUMENT_MUST_BE_CONST, ARGUMENT_TYPE_MISMATCH!>Test()<!>)
class Test2
