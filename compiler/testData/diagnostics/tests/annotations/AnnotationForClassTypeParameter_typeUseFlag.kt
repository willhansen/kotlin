// !LANGUAGE: +ProperCheckAnnotationsTargetInTypeUsePositions -ClassTypeParameterAnnotations

annotation class A1
annotation class A2(konst some: Int = 12)

@Target(AnnotationTarget.TYPE)
annotation class TA1

@Target(AnnotationTarget.TYPE_PARAMETER)
annotation class TA2(konst some: Int = 12)

class TopLevelClass<<!UNSUPPORTED_FEATURE, WRONG_ANNOTATION_TARGET!>@A1<!> <!UNSUPPORTED_FEATURE, WRONG_ANNOTATION_TARGET!>@A2(3)<!> <!UNSUPPORTED_FEATURE, WRONG_ANNOTATION_TARGET!>@A2<!> <!UNSUPPORTED_FEATURE, WRONG_ANNOTATION_TARGET!>@A1(12)<!> <!UNSUPPORTED_FEATURE, WRONG_ANNOTATION_TARGET!>@A2("Test")<!> T> {
    class InnerClass<<!UNSUPPORTED_FEATURE, WRONG_ANNOTATION_TARGET!>@A1<!> <!UNSUPPORTED_FEATURE, WRONG_ANNOTATION_TARGET!>@A2(3)<!> <!UNSUPPORTED_FEATURE, WRONG_ANNOTATION_TARGET!>@A2<!> <!UNSUPPORTED_FEATURE, WRONG_ANNOTATION_TARGET!>@A1(12)<!> <!UNSUPPORTED_FEATURE, WRONG_ANNOTATION_TARGET!>@A2("Test")<!> T> {
        fun test() {
            class InFun<<!UNSUPPORTED_FEATURE, WRONG_ANNOTATION_TARGET!>@A1<!> <!UNSUPPORTED_FEATURE, WRONG_ANNOTATION_TARGET!>@A2(3)<!> <!UNSUPPORTED_FEATURE, WRONG_ANNOTATION_TARGET!>@A2<!> <!UNSUPPORTED_FEATURE, WRONG_ANNOTATION_TARGET!>@A1(12)<!> <!UNSUPPORTED_FEATURE, WRONG_ANNOTATION_TARGET!>@A2("Test")<!> T>
        }
    }
}

class TTopLevelClass<<!UNSUPPORTED_FEATURE, WRONG_ANNOTATION_TARGET!>@TA1<!> <!UNSUPPORTED_FEATURE, WRONG_ANNOTATION_TARGET!>@TA2(3)<!> <!UNSUPPORTED_FEATURE, WRONG_ANNOTATION_TARGET!>@TA2<!> <!UNSUPPORTED_FEATURE, WRONG_ANNOTATION_TARGET!>@TA1(12)<!> <!UNSUPPORTED_FEATURE, WRONG_ANNOTATION_TARGET!>@TA2("Test")<!> T> {
    class TInnerClass<<!UNSUPPORTED_FEATURE, WRONG_ANNOTATION_TARGET!>@TA1<!> <!UNSUPPORTED_FEATURE, WRONG_ANNOTATION_TARGET!>@TA2(3)<!> <!UNSUPPORTED_FEATURE, WRONG_ANNOTATION_TARGET!>@TA2<!> <!UNSUPPORTED_FEATURE, WRONG_ANNOTATION_TARGET!>@TA1(12)<!> <!UNSUPPORTED_FEATURE, WRONG_ANNOTATION_TARGET!>@TA2("Test")<!> T> {
        fun test() {
            class TInFun<<!UNSUPPORTED_FEATURE, WRONG_ANNOTATION_TARGET!>@TA1<!> <!UNSUPPORTED_FEATURE, WRONG_ANNOTATION_TARGET!>@TA2(3)<!> <!UNSUPPORTED_FEATURE, WRONG_ANNOTATION_TARGET!>@TA2<!> <!UNSUPPORTED_FEATURE, WRONG_ANNOTATION_TARGET!>@TA1(12)<!> <!UNSUPPORTED_FEATURE, WRONG_ANNOTATION_TARGET!>@TA2("Test")<!> T>
        }
    }
}
