// CURIOUS_ABOUT describeContents
// WITH_STDLIB

import kotlinx.android.parcel.*
import android.os.Parcelable

abstract class AbstractUser : Parcelable {
    override fun describeContents() = 100
}

@Parcelize
class User(konst firstName: String, konst lastName: String, konst age: Int, konst isProUser: Boolean) : AbstractUser()