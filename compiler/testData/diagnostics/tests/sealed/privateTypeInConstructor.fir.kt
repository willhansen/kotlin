// ISSUE: KT-45043, KT-51229
// DIAGNOSTICS: -UNUSED_PARAMETER

private class Bar

sealed class SealedFoo(
    konst <!EXPOSED_PROPERTY_TYPE_IN_CONSTRUCTOR_ERROR!>x<!>: Bar,
    private konst y: Bar,
    z: Bar
)

abstract class AbstractFoo(
    <!EXPOSED_PARAMETER_TYPE!>konst <!EXPOSED_PROPERTY_TYPE_IN_CONSTRUCTOR_ERROR!>x<!>: Bar<!>,
    <!EXPOSED_PARAMETER_TYPE!>private konst y: Bar<!>,
    <!EXPOSED_PARAMETER_TYPE!>z: Bar<!>
)

internal sealed class A {
    protected abstract konst b: B?
    protected data class B(konst s: String)
    internal data class C private constructor(override konst b: B?) : A() {
        constructor() : this(null)
    }
}