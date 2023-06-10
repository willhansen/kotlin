// CURIOUS_ABOUT writeToParcel, createFromParcel, <clinit>, describeContents
// WITH_STDLIB
// LOCAL_VARIABLE_TABLE

import kotlinx.android.parcel.*
import android.os.Parcelable

@Parcelize
class User(konst firstName: String, konst lastName: String, konst age: Int, konst isProUser: Boolean) : Parcelable