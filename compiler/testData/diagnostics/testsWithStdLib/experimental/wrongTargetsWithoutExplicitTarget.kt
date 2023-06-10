// FIR_IDENTICAL
// !OPT_IN: kotlin.RequiresOptIn

@RequiresOptIn
annotation class SomeOptInMarker

@RequiresOptIn
@Target(AnnotationTarget.VALUE_PARAMETER, AnnotationTarget.PROPERTY, AnnotationTarget.PROPERTY_GETTER, AnnotationTarget.LOCAL_VARIABLE)
annotation class OtherOptInMarker

class IntWrapper(
    <!OPT_IN_MARKER_ON_WRONG_TARGET!>@SomeOptInMarker<!>
    <!OPT_IN_MARKER_ON_WRONG_TARGET!>@OtherOptInMarker<!>
    konst konstue: Int
) {
    konst isEven: Boolean
        <!OPT_IN_MARKER_ON_WRONG_TARGET!>@SomeOptInMarker<!>
        <!OPT_IN_MARKER_ON_WRONG_TARGET!>@OtherOptInMarker<!>
        get() = (konstue % 2) == 0
}

fun foo() {
    <!OPT_IN_MARKER_ON_WRONG_TARGET!>@SomeOptInMarker<!>
    <!OPT_IN_MARKER_ON_WRONG_TARGET!>@OtherOptInMarker<!>
    konst konstue = 2
}