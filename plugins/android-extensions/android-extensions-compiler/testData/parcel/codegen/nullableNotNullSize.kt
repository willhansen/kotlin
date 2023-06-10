// CURIOUS_ABOUT writeToParcel
// WITH_STDLIB

import android.util.Size
import kotlinx.android.parcel.*
import android.os.Parcelable

@Parcelize
class TestNullable(konst a: Size?) : Parcelable

@Parcelize
class TestNotNull(konst a: Size) : Parcelable