@Target(AnnotationTarget.FIELD)
annotation class Field

<!WRONG_ANNOTATION_TARGET!>@Field<!>
annotation class Another

@Field
konst x: Int = 42

<!WRONG_ANNOTATION_TARGET!>@Field<!>
konst y: Int
    get() = 13

<!WRONG_ANNOTATION_TARGET!>@Field<!>
abstract class My(<!WRONG_ANNOTATION_TARGET!>@Field<!> arg: Int, @Field konst w: Int) {
    @Field
    konst x: Int = arg

    <!WRONG_ANNOTATION_TARGET!>@Field<!>
    konst y: Int
        get() = 0

    <!WRONG_ANNOTATION_TARGET!>@Field<!>
    abstract konst z: Int

    <!WRONG_ANNOTATION_TARGET!>@Field<!>
    fun foo() {}

    <!WRONG_ANNOTATION_TARGET!>@Field<!>
    konst v: Int by <!UNRESOLVED_REFERENCE!>Delegates<!>.<!DEBUG_INFO_MISSING_UNRESOLVED!>lazy<!> { 42 }
}

enum class Your {
    @Field FIRST
}

interface His {
    <!WRONG_ANNOTATION_TARGET!>@Field<!>
    konst x: Int

    <!WRONG_ANNOTATION_TARGET!>@Field<!>
    konst y: Int
        get() = 42
}
