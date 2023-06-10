// FIR_IDENTICAL
// !CHECK_TYPE
// !DIAGNOSTICS: -UNUSED_PARAMETER
data class A(konst x: Int, konst y: String)

fun foo(block: (A) -> Unit) { }

@Target(AnnotationTarget.VALUE_PARAMETER)
annotation class Ann

fun bar() {
    foo { (<!WRONG_MODIFIER_TARGET!>private<!> <!WRONG_MODIFIER_TARGET!>inline<!> a, <!WRONG_ANNOTATION_TARGET!>@Ann<!> b) ->
        a checkType { _<Int>() }
        b checkType { _<String>() }
    }
}
