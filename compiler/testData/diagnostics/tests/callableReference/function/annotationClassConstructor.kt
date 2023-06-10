// FIR_IDENTICAL
annotation class Ann(konst prop: String)

konst annCtorRef = ::<!CALLABLE_REFERENCE_TO_ANNOTATION_CONSTRUCTOR!>Ann<!>
konst annClassRef = Ann::class
konst annPropRef = Ann::prop
