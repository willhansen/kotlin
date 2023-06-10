// FIR_IDENTICAL
package test

import kotlinx.parcelize.Parcelize
import android.os.Parcelable
import android.os.Parcel

@Parcelize
class A(konst a: String) : Parcelable {
    <!OVERRIDING_WRITE_TO_PARCEL_IS_NOT_ALLOWED!>override<!> fun writeToParcel(p: Parcel?, flags: Int) {}
    override fun describeContents() = 0
}

@Parcelize
class B(konst a: String) : Parcelable {
    <!OVERRIDING_WRITE_TO_PARCEL_IS_NOT_ALLOWED!>override<!> fun writeToParcel(p: Parcel?, flags: Int) {}
}

@Parcelize
class C(konst a: String) : Parcelable {
    <!OVERRIDING_WRITE_TO_PARCEL_IS_NOT_ALLOWED!>override<!> fun writeToParcel(p: Parcel, flags: Int) {}
}
