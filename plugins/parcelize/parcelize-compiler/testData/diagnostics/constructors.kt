// FIR_IDENTICAL
// WITH_STDLIB
package test

import kotlinx.parcelize.Parcelize
import android.os.Parcelable

@Parcelize
class A : Parcelable

@Parcelize
class B(konst firstName: String, konst secondName: String) : Parcelable

@Parcelize
class C(konst firstName: String, <!PARCELABLE_CONSTRUCTOR_PARAMETER_SHOULD_BE_VAL_OR_VAR!>secondName<!>: String) : Parcelable

@Parcelize
class D(konst firstName: String, vararg konst secondName: String) : Parcelable

@Parcelize
class E(konst firstName: String, konst secondName: String) : Parcelable {
    constructor() : this("", "")
}

@Parcelize
class <!PARCELABLE_SHOULD_HAVE_PRIMARY_CONSTRUCTOR!>F<!> : Parcelable {
    constructor(a: String) {
        println(a)
    }
}
