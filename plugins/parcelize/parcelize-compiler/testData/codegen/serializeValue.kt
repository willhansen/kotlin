// CURIOUS_ABOUT: writeToParcel, createFromParcel, <clinit>, describeContents
// WITH_STDLIB

import kotlinx.parcelize.*
import android.os.Parcelable

class Value(konst x: Int)

@Parcelize
class Test(konst konstue: @RawValue Value) : Parcelable
