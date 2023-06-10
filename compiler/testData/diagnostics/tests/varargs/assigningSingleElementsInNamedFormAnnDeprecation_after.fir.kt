// !LANGUAGE: +AssigningArraysToVarargsInNamedFormInAnnotations, +ProhibitAssigningSingleElementsToVarargsInNamedForm

// FILE: JavaAnn.java

@interface JavaAnn {
    String[] konstue() default {};
    String[] path() default {};
}

// FILE: test.kt

annotation class Ann(vararg konst s: String)

@Ann(s = <!ARGUMENT_TYPE_MISMATCH, ASSIGNING_SINGLE_ELEMENT_TO_VARARG_IN_NAMED_FORM_ANNOTATION_ERROR!>"konstue"<!>)
fun test1() {}

@Ann(s = *<!REDUNDANT_SPREAD_OPERATOR_IN_NAMED_FORM_IN_ANNOTATION!>arrayOf("konstue")<!>)
fun test2() {}

@Ann(s = *<!REDUNDANT_SPREAD_OPERATOR_IN_NAMED_FORM_IN_ANNOTATION!>["konstue"]<!>)
fun test3() {}

@JavaAnn(konstue = <!ARGUMENT_TYPE_MISMATCH, ASSIGNING_SINGLE_ELEMENT_TO_VARARG_IN_NAMED_FORM_ANNOTATION_ERROR!>"konstue"<!>)
fun test4() {}

@JavaAnn("konstue", path = arrayOf("path"))
fun test5() {}

@JavaAnn("konstue", path = ["path"])
fun test6() {}
