// CURIOUS_ABOUT writeToParcel
// WITH_STDLIB

import kotlinx.android.parcel.*
import android.os.Parcelable

@Parcelize
class Test(konst names: List<String>) : Parcelable