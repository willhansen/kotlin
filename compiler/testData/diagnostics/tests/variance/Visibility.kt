// FIR_IDENTICAL
interface Test<in I, out O> {
    konst internal_konst: <!TYPE_VARIANCE_CONFLICT_ERROR!>I<!>
    public konst public_konst: <!TYPE_VARIANCE_CONFLICT_ERROR!>I<!>
    <!WRONG_MODIFIER_CONTAINING_DECLARATION!>protected<!> konst protected_konst: <!TYPE_VARIANCE_CONFLICT_ERROR!>I<!>
    <!PRIVATE_PROPERTY_IN_INTERFACE!>private<!> konst private_konst: I

    var interlan_private_set: <!TYPE_VARIANCE_CONFLICT_ERROR!>O<!>
        <!PRIVATE_SETTER_FOR_ABSTRACT_PROPERTY!>private<!> set
    public var public_private_set: <!TYPE_VARIANCE_CONFLICT_ERROR!>O<!>
        <!PRIVATE_SETTER_FOR_ABSTRACT_PROPERTY!>private<!> set
    <!WRONG_MODIFIER_CONTAINING_DECLARATION!>protected<!> var protected_private_set: <!TYPE_VARIANCE_CONFLICT_ERROR!>O<!>
        <!PRIVATE_SETTER_FOR_ABSTRACT_PROPERTY!>private<!> set
    <!PRIVATE_PROPERTY_IN_INTERFACE!>private<!> var private_private_set: O
        private set

    fun internal_fun(i: <!TYPE_VARIANCE_CONFLICT_ERROR!>O<!>) : <!TYPE_VARIANCE_CONFLICT_ERROR!>I<!>
    public fun public_fun(i: <!TYPE_VARIANCE_CONFLICT_ERROR!>O<!>) : <!TYPE_VARIANCE_CONFLICT_ERROR!>I<!>
    <!WRONG_MODIFIER_CONTAINING_DECLARATION!>protected<!> fun protected_fun(i: <!TYPE_VARIANCE_CONFLICT_ERROR!>O<!>) : <!TYPE_VARIANCE_CONFLICT_ERROR!>I<!>
    <!PRIVATE_FUNCTION_WITH_NO_BODY!>private<!> fun private_fun(i: O) : I
}
