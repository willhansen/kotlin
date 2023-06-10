// FIR_IDENTICAL
annotation class base

konst x: List<<!WRONG_ANNOTATION_TARGET!>@base<!> String>? = null

konst y: List<@[<!WRONG_ANNOTATION_TARGET!>base<!>] String>? = null

@Target(AnnotationTarget.TYPE)
annotation class typeAnn

fun foo(list: List<@typeAnn Int>) = list