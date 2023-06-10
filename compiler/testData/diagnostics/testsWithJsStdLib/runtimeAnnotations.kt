// FIR_IDENTICAL
// !DIAGNOSTICS: +RUNTIME_ANNOTATION_NOT_SUPPORTED
@Retention(AnnotationRetention.BINARY)
annotation class X

@Retention(AnnotationRetention.RUNTIME)
annotation class Y

@X
external class A {
    @X
    fun f()

    @X
    konst p: Int

    @get:X
    konst r: Int
}

<!RUNTIME_ANNOTATION_ON_EXTERNAL_DECLARATION!>@Y<!>
external class B {
    <!RUNTIME_ANNOTATION_ON_EXTERNAL_DECLARATION!>@Y<!>
    fun f()

    <!RUNTIME_ANNOTATION_ON_EXTERNAL_DECLARATION!>@Y<!>
    konst p: Int

    <!RUNTIME_ANNOTATION_ON_EXTERNAL_DECLARATION!>@get:Y<!>
    konst r: Int
}

typealias TY = Y

<!RUNTIME_ANNOTATION_ON_EXTERNAL_DECLARATION!>@TY<!>
external class BB

@X
class C {
    @X
    fun f() {}

    @X
    konst p: Int = 0

    konst q: Int
        @X get() = 0

    @get:X
    konst r: Int = 0
}

<!RUNTIME_ANNOTATION_NOT_SUPPORTED!>@Y<!>
class D {
    <!RUNTIME_ANNOTATION_NOT_SUPPORTED!>@Y<!>
    fun f() {}

    <!RUNTIME_ANNOTATION_NOT_SUPPORTED!>@Y<!>
    konst p: Int = 0

    konst q: Int
      <!RUNTIME_ANNOTATION_NOT_SUPPORTED!>@Y<!> get() = 0

    <!RUNTIME_ANNOTATION_NOT_SUPPORTED!>@get:Y<!>
    konst r: Int = 0
}