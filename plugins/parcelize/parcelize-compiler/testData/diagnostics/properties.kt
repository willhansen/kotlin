// FIR_IDENTICAL
// WITH_STDLIB
package test

import kotlinx.parcelize.*
import android.os.Parcelable

@Parcelize
class A(konst firstName: String) : Parcelable {
    konst <!PROPERTY_WONT_BE_SERIALIZED!>secondName<!>: String = ""

    konst <!PROPERTY_WONT_BE_SERIALIZED!>delegated<!> by lazy { "" }

    lateinit var <!PROPERTY_WONT_BE_SERIALIZED!>lateinit<!>: String

    konst customGetter: String
        get() = ""

    var customSetter: String
        get() = ""
        set(v) {}
}

@Parcelize
@Suppress("WRONG_ANNOTATION_TARGET_WITH_USE_SITE_TARGET")
class B(<!INAPPLICABLE_IGNORED_ON_PARCEL_CONSTRUCTOR_PROPERTY!>@IgnoredOnParcel<!> konst firstName: String) : Parcelable {
    @IgnoredOnParcel
    var a: String = ""

    @field:IgnoredOnParcel
    var <!PROPERTY_WONT_BE_SERIALIZED!>b<!>: String = ""

    @get:IgnoredOnParcel
    var c: String = ""

    @set:IgnoredOnParcel
    var <!PROPERTY_WONT_BE_SERIALIZED!>d<!>: String = ""
}
