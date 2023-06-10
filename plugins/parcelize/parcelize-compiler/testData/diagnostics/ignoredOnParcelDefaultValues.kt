// FIR_IDENTICAL
package test

import android.os.Parcelable
import kotlinx.parcelize.Parcelize
import kotlinx.parcelize.IgnoredOnParcel

@Parcelize
class A(@IgnoredOnParcel konst x: String = "OK") : Parcelable
