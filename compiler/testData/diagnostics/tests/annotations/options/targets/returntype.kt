// FIR_IDENTICAL
annotation class base

@Target(AnnotationTarget.TYPE)
annotation class typed

@base class My(konst x: <!WRONG_ANNOTATION_TARGET!>@base<!> @typed Int, y: <!WRONG_ANNOTATION_TARGET!>@base<!> @typed Int) {
    konst z: <!WRONG_ANNOTATION_TARGET!>@base<!> @typed Int = y
    fun foo(): <!WRONG_ANNOTATION_TARGET!>@base<!> @typed Int = z
}
